package io.github.lord1egypt.prootx.support

import android.os.Build
import java.io.File
import java.security.MessageDigest
import java.util.Properties

class SupportIntegrityException(message: String) : IllegalStateException(message)

enum class SupportLane { LEGACY, MODERN }

data class SupportInstallation(
    val abi: String,
    val release: String,
    val lane: SupportLane,
    val commonNames: Set<String>,
    val legacyNames: Set<String>,
    val modernNames: Set<String>
) {
    /**
     * Logical runtime names this installation actually exposes: the common payload plus the
     * native lane selected for the host. The v1.2.0 payload is lane-asymmetric (e.g. legacy
     * `libcrypto.so.1.1` vs modern `libcrypto.so.3`), so the inactive lane must never be
     * required to exist — otherwise the marker fast path can never match and every startup
     * reinstalls the support state.
     */
    val runtimeNames: Set<String>
        get() = commonNames + when (lane) {
            SupportLane.LEGACY -> legacyNames
            SupportLane.MODERN -> modernNames
        }

    /** Every logical name the support map declares for this ABI, across both lanes. */
    val allDeclaredNames: Set<String>
        get() = commonNames + legacyNames + modernNames
}

object AbiResolver {
    /** First device ABI (Android preference order) that the support map provides. */
    fun select(supportedAbis: List<String>, deviceAbis: List<String>): String? =
        deviceAbis.firstOrNull { supportedAbis.contains(it) }
}

fun sha256Hex(bytes: ByteArray): String {
    val md = MessageDigest.getInstance("SHA-256")
    return md.digest(bytes).joinToString("") { "%02x".format(it) }
}

/**
 * Manifest-driven support runtime installer/resolver.
 *
 * API 21-28: copies the selected ABI's frozen legacy native payload plus the common
 * scripts/data out of APK assets into `filesDir/support` (integrity-checked).
 *
 * API 29+: copies the common scripts/data and creates logical symlinks in
 * `filesDir/support` pointing at the modern native payload in `nativeLibraryDir`.
 * No modern executable is ever copied into writable storage (Android W^X).
 */
class SupportRuntimeInstaller(
    private val assetSource: SupportAssetSource,
    private val nativeLibraryDir: File,
    private val sdkInt: Int,
    private val deviceAbis: List<String>,
    private val symlinker: Symlinker = Symlinker.default(),
    private val hashProvider: (ByteArray) -> String = ::sha256Hex
) {

    companion object {
        const val MARKER_NAME = ".prootx-support-install"

        /**
         * Names produced by the historical v1.0.0/v1.1.0 flat layout. They are reconciled
         * away on upgrade so a stale symlink can never shadow a new lane mapping.
         */
        val HISTORICAL_NAMES: Set<String> = setOf(
            "proot", "loader", "loader32", "busybox", "busybox_static", "dbclient",
            "proot_meta", "proot_meta_leveldb", "libcrypto.so.1.1", "libleveldb.so.1",
            "libtalloc.so.2", "libtermux-auth.so", "libutil.so", "libc++_shared.so",
            "libandroid-shmem.so",
            "addNonRootUser.sh", "compressFilesystem.sh", "deleteFilesystem.sh",
            "execInProot.sh", "extractFilesystem.sh", "isServerInProcTree.sh",
            "killProcTree.sh", "stat4", "stat8", "uptime"
        )
    }

    fun install(supportDir: File): SupportInstallation =
        install(supportDir, SupportMapLoader.load(assetSource))

    fun install(supportDir: File, map: SupportMap): SupportInstallation {
        val abi = AbiResolver.select(map.supportedAbis, deviceAbis)
            ?: throw SupportMapException(
                "device ABIs $deviceAbis are not supported by ${map.supportedAbis}"
            )
        val abiEntry = map.abi(abi)
        val lane = if (sdkInt >= Build.VERSION_CODES.Q) SupportLane.MODERN else SupportLane.LEGACY

        supportDir.mkdirs()

        val installation = SupportInstallation(
            abi = abi,
            release = map.release,
            lane = lane,
            commonNames = map.common.map { it.name }.toSet(),
            legacyNames = abiEntry.legacy.map { it.name }.toSet(),
            modernNames = abiEntry.modern.map { it.name }.toSet()
        )

        if (isAlreadyInstalled(supportDir, installation)) return installation

        reconcile(supportDir, map, abiEntry)
        installCommon(supportDir, map.common)
        when (lane) {
            SupportLane.MODERN -> linkModern(supportDir, abiEntry.modern)
            SupportLane.LEGACY -> installLegacy(supportDir, abiEntry.legacy)
        }
        writeMarker(supportDir, installation)
        return installation
    }

    private fun managedNames(map: SupportMap, abi: SupportAbi): Set<String> = buildSet {
        map.common.forEach { add(it.name) }
        abi.legacy.forEach { add(it.name) }
        abi.modern.forEach { add(it.name) }
        addAll(HISTORICAL_NAMES)
    }

    private fun reconcile(supportDir: File, map: SupportMap, abi: SupportAbi) {
        val managed = managedNames(map, abi)
        supportDir.listFiles()?.forEach { f ->
            if (f.name in managed) f.deleteRecursively()
        }
    }

    private fun installCommon(supportDir: File, files: List<CommonFile>) {
        files.forEach { f ->
            copyVerified(f.assetPath, File(supportDir, f.name), f.sha256, f.executable)
        }
    }

    private fun installLegacy(supportDir: File, files: List<LegacyFile>) {
        files.forEach { f ->
            copyVerified(f.assetPath, File(supportDir, f.name), f.sha256, f.executable)
        }
    }

    private fun linkModern(supportDir: File, files: List<ModernFile>) {
        files.forEach { f ->
            val target = File(nativeLibraryDir, f.nativeLib)
            if (!target.isFile) {
                throw SupportMapException(
                    "modern native '${f.nativeLib}' is missing from nativeLibraryDir"
                )
            }
            val link = File(supportDir, f.name)
            link.delete()
            symlinker.createSymlink(target.path, link.path)
        }
    }

    private fun copyVerified(assetPath: String, dest: File, expectedSha: String, executable: Boolean) {
        val bytes = assetSource.open(assetPath).use { it.readBytes() }
        val actual = hashProvider(bytes)
        if (actual != expectedSha) {
            throw SupportIntegrityException(
                "support payload '$assetPath' failed integrity check: expected $expectedSha, got $actual"
            )
        }
        dest.parentFile?.mkdirs()
        dest.writeBytes(bytes)
        dest.setExecutable(executable, false)
    }

    private fun marker(supportDir: File) = File(supportDir, MARKER_NAME)

    private fun isAlreadyInstalled(supportDir: File, installation: SupportInstallation): Boolean {
        val m = marker(supportDir)
        if (!m.isFile) return false
        val props = Properties()
        m.inputStream().use { props.load(it) }
        if (props.getProperty("release") != installation.release) return false
        if (props.getProperty("abi") != installation.abi) return false
        if (props.getProperty("lane") != installation.lane.name) return false
        return installation.runtimeNames.all { File(supportDir, it).exists() }
    }

    private fun writeMarker(supportDir: File, installation: SupportInstallation) {
        val props = Properties()
        props.setProperty("release", installation.release)
        props.setProperty("abi", installation.abi)
        props.setProperty("lane", installation.lane.name)
        marker(supportDir).outputStream().use { props.store(it, "ProotX support installation") }
    }
}

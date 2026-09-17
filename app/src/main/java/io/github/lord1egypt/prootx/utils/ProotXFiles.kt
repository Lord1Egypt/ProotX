package io.github.lord1egypt.prootx.utils

import android.content.Context
import android.os.Build
import io.github.lord1egypt.prootx.support.AndroidSupportAssetSource
import io.github.lord1egypt.prootx.support.SupportInstallation
import io.github.lord1egypt.prootx.support.SupportRuntimeInstaller
import io.github.lord1egypt.prootx.support.Symlinker
import java.io.File

/**
 * Resolves and installs the ProotX support runtime from the pinned v1.2.0 support bundle.
 *
 * The installation is driven entirely by the generated `support-map.json` routing contract:
 * the API 21-28 lane extracts the frozen legacy payload from assets, the API 29+ lane
 * links to the modern payload in `nativeLibraryDir`. There is no filename heuristics and no
 * `lib_arch.so` pseudo-native marker.
 */
class ProotXFiles(
    context: Context,
    libDirPath: String,
    private val symlinker: Symlinker = Symlinker.default(),
    installerOverride: SupportRuntimeInstaller? = null
) {

    val filesDir: File = context.filesDir
    val libDir: File = File(libDirPath)
    val supportDir: File = File(filesDir, "support")
    val emulatedScopedDir = context.getExternalFilesDir(null)!!
    val emulatedUserDir = File(emulatedScopedDir, "storage")

    val sdCardScopedDir: File? = resolveSdCardScopedStorage(context)
    val sdCardUserDir: File? = if (sdCardScopedDir != null) {
        File(sdCardScopedDir, "storage")
    } else null

    val busybox = File(supportDir, "busybox")
    val proot = File(supportDir, "proot")

    private val installer: SupportRuntimeInstaller = installerOverride ?: SupportRuntimeInstaller(
        assetSource = AndroidSupportAssetSource(context),
        nativeLibraryDir = libDir,
        sdkInt = Build.VERSION.SDK_INT,
        deviceAbis = Build.SUPPORTED_ABIS.toList(),
        symlinker = symlinker
    )

    val installation: SupportInstallation

    init {
        emulatedUserDir.mkdirs()
        sdCardUserDir?.mkdirs()

        installation = installer.install(supportDir)
    }

    fun makePermissionsUsable(containingDirectoryPath: String, filename: String) {
        val commandToRun = arrayListOf("chmod", "0777", filename)

        val containingDirectory = File(containingDirectoryPath)
        containingDirectory.mkdirs()

        val pb = ProcessBuilder(commandToRun)
        pb.directory(containingDirectory)

        val process = pb.start()
        process.waitFor()
    }

    private fun resolveSdCardScopedStorage(context: Context): File? {
        // Allegedly returns at most 2 elements, if there is a physical external storage device,
        // according to https://developer.android.com/training/data-storage/files at
        // 'Select between multiple storage locations'
        val externals = context.getExternalFilesDirs(null)
        return if (externals.size > 1) {
            externals[1]
        } else null
    }

    /** Selected support ABI in ProotX's short naming (e.g. `arm64`, `x86_64`). */
    fun getArchType(): String {
        return translateABI(installation.abi)
    }

    private fun translateABI(abi: String): String {
        return when (abi) {
            "arm64-v8a" -> "arm64"
            "armeabi-v7a" -> "arm"
            "x86_64" -> "x86_64"
            "x86" -> "x86"
            else -> ""
        }
    }
}

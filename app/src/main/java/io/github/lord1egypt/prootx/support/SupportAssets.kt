package io.github.lord1egypt.prootx.support

import android.content.Context
import com.squareup.moshi.Moshi
import java.io.File
import java.io.InputStream

/** Abstraction over the APK asset tree so support routing is unit-testable. */
interface SupportAssetSource {
    fun open(path: String): InputStream
    fun list(path: String): List<String>
}

class AndroidSupportAssetSource(private val context: Context) : SupportAssetSource {
    override fun open(path: String): InputStream = context.assets.open(path)

    override fun list(path: String): List<String> = context.assets.list(path)?.toList() ?: emptyList()
}

class FileSupportAssetSource(private val root: File) : SupportAssetSource {
    override fun open(path: String): InputStream {
        val f = File(root, path)
        if (!f.isFile) throw SupportMapException("missing support asset: $path")
        return f.inputStream()
    }

    override fun list(path: String): List<String> =
        File(root, path).list()?.toList() ?: emptyList()
}

object SupportRoutingValidator {
    /** Runtime routes the application hard-requires on every supported ABI. */
    val REQUIRED_ROUTES: Set<String> = setOf(
        "proot", "loader", "proot_meta", "proot_meta_leveldb",
        "busybox", "busybox_static", "dbclient", "execInProot.sh"
    )

    fun validate(map: SupportMap) {
        if (map.schemaVersion != 1) throw SupportMapException("unsupported support map schema ${map.schemaVersion}")
        if (map.supportedAbis.isEmpty()) throw SupportMapException("support map lists no supported ABIs")
        map.supportedAbis.forEach { abi ->
            if (!map.abis.containsKey(abi)) throw SupportMapException("support map missing ABI '$abi'")
        }
        if (map.common.isEmpty()) throw SupportMapException("support map has no common files")
        map.abis.forEach { (abi, entry) ->
            // A logical name legitimately has a legacy and a modern variant. Duplicates are
            // only invalid within a single lane.
            val duplicates = listOf(
                map.common.map { it.name },
                entry.legacy.map { it.name },
                entry.modern.map { it.name }
            ).flatMap { lane -> lane.groupingBy { it }.eachCount().filterValues { it > 1 }.keys }
            if (duplicates.isNotEmpty()) {
                throw SupportMapException("duplicate runtime name(s) for $abi: ${duplicates.sorted()}")
            }
            val names = mutableListOf<String>()
            names += map.common.map { it.name }
            names += entry.legacy.map { it.name }
            names += entry.modern.map { it.name }
            val present = names.toSet()
            val missing = REQUIRED_ROUTES - present
            if (missing.isNotEmpty()) {
                throw SupportMapException("required runtime route(s) missing for $abi: ${missing.sorted()}")
            }
            if (entry.modern.isEmpty() && entry.legacy.isEmpty()) {
                throw SupportMapException("ABI $abi has no legacy or modern payload")
            }
        }
    }
}

object SupportMapLoader {
    const val MAP_ASSET_PATH = "support/metadata/support-map.json"

    private val moshi: Moshi by lazy { Moshi.Builder().build() }

    fun load(source: SupportAssetSource): SupportMap {
        val json = source.open(MAP_ASSET_PATH).use { it.readBytes().toString(Charsets.UTF_8) }
        val adapter = moshi.adapter(SupportMap::class.java)
        val map = adapter.fromJson(json) ?: throw SupportMapException("empty support map")
        SupportRoutingValidator.validate(map)
        return map
    }
}

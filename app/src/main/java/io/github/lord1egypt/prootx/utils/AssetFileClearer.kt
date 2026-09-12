package io.github.lord1egypt.prootx.utils

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class AssetFileClearer(
    private val prootxFiles: ProotXFiles,
    private val assetDirectoryNames: Set<String>,
    private val busyboxExecutor: BusyboxExecutor,
    private val logger: Logger = SentryLogger()
) {
    @Throws(FileNotFoundException::class, IllegalStateException::class)
    suspend fun clearAllSupportAssets() {
        if (!prootxFiles.filesDir.exists()) {
            val exception = FileNotFoundException()
            logger.addExceptionBreadcrumb(exception)
            throw exception
        }
        if (!prootxFiles.busybox.exists()) {
            val exception = IllegalStateException("Busybox missing")
            logger.addExceptionBreadcrumb(exception)
            throw exception
        }
        clearFilesystemSupportAssets()
        clearTopLevelAssets(assetDirectoryNames)
    }

    @Throws(IOException::class)
    private suspend fun clearTopLevelAssets(assetDirectoryNames: Set<String>) {
        val files = prootxFiles.filesDir.listFiles() ?: return
        for (file in files) {
            if (!file.isDirectory) continue
            if (!assetDirectoryNames.contains(file.name)) continue
            if (file.name == "support") continue
            if (busyboxExecutor.recursivelyDelete(file.absolutePath) !is SuccessfulExecution) {
                val exception = IOException()
                logger.addExceptionBreadcrumb(exception)
                throw exception
            }
        }
    }

    @Throws(IOException::class)
    private suspend fun clearFilesystemSupportAssets() {
        val files = prootxFiles.filesDir.listFiles() ?: return
        for (file in files) {
            if (!file.isDirectory || file.name.toIntOrNull() == null) continue

            val supportDirectory = File("${file.absolutePath}/support")
            if (!supportDirectory.exists() || !supportDirectory.isDirectory) continue

            val supportFiles = supportDirectory.listFiles() ?: continue
            for (supportFile in supportFiles) {
                // Exclude directories and hidden files.
                if (supportFile.isDirectory || supportFile.name.first() == '.') continue
                // Use deleteRecursively to match functionality above
                if (busyboxExecutor.recursivelyDelete(supportFile.path) !is SuccessfulExecution) {
                    val exception = IOException()
                    logger.addExceptionBreadcrumb(exception)
                    throw exception
                }
            }
        }
    }
}
package io.github.lord1egypt.prootx.utils

import android.app.DownloadManager

/**
 * Pure validation for `DownloadManager.ACTION_DOWNLOAD_COMPLETE` broadcasts.
 *
 * The completion receiver is registered `RECEIVER_EXPORTED` on API 33+ because the
 * DownloadManager provider is a separate process; any app can therefore send a matching
 * intent. Only a matching action carrying a real download id is accepted here. The id is
 * then validated against this application's own enqueued downloads by
 * `AssetDownloader.handleDownloadComplete` (`NonProotXDownloadFound` for anything unknown)
 * before any trusted processing happens.
 */
object DownloadCompletion {
    fun extractDownloadId(action: String?, downloadId: Long): Long? =
        if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE && downloadId != -1L) downloadId else null
}

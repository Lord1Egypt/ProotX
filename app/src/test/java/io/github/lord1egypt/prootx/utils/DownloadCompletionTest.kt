package io.github.lord1egypt.prootx.utils

import android.app.DownloadManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The download completion receiver is registered `RECEIVER_EXPORTED` on API 33+ (the
 * DownloadManager provider is a separate process), so only a matching action carrying a real
 * download id may proceed. Unknown ids are then rejected by
 * `AssetDownloader.handleDownloadComplete` (`NonProotXDownloadFound`).
 */
class DownloadCompletionTest {

    @Test
    fun `accepts a matching action with a real id`() {
        assertEquals(42L, DownloadCompletion.extractDownloadId(DownloadManager.ACTION_DOWNLOAD_COMPLETE, 42L))
    }

    @Test
    fun `rejects a mismatched action`() {
        assertNull(DownloadCompletion.extractDownloadId("android.intent.action.SOMETHING_ELSE", 42L))
    }

    @Test
    fun `rejects a null action`() {
        assertNull(DownloadCompletion.extractDownloadId(null, 42L))
    }

    @Test
    fun `rejects the missing-id sentinel`() {
        assertNull(DownloadCompletion.extractDownloadId(DownloadManager.ACTION_DOWNLOAD_COMPLETE, -1L))
    }
}

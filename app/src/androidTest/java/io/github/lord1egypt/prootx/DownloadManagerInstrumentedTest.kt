package io.github.lord1egypt.prootx

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.net.ServerSocket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Real DownloadManager runtime gate: a genuine completion broadcast from the DownloadManager
 * provider must be delivered to the modern (explicit export) registration, proving the API36
 * fix works against the real provider rather than a synthetic broadcast.
 *
 * The DownloadManager provider is a separate process, so the receiver is registered
 * `RECEIVER_EXPORTED` on API 33+, exactly as `MainActivity` does.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class DownloadManagerInstrumentedTest {

    @Test
    fun realDownloadCompletionIsDelivered() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val body = "prootx-download-complete".toByteArray()
        val server = ServerSocket(0)
        val port = server.localPort
        Thread {
            try {
                val client = server.accept()
                val out = client.getOutputStream()
                out.write("HTTP/1.1 200 OK\r\nContent-Length: ${body.size}\r\nConnection: close\r\n\r\n".toByteArray())
                out.write(body)
                out.flush()
                client.close()
            } catch (_: Exception) {
                // the assertion below reports delivery failure
            }
        }.apply { isDaemon = true }.start()

        val downloadManager = context.getSystemService(DownloadManager::class.java)
        val request = DownloadManager.Request(Uri.parse("http://127.0.0.1:$port/dl.txt"))
            .setDestinationInExternalFilesDir(context, null, "p1f5-dl.txt")
        val enqueuedId = downloadManager.enqueue(request)

        val latch = CountDownLatch(1)
        var receivedId = -1L
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
                receivedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                latch.countDown()
            }
        }
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }

        try {
            assertTrue("no DownloadManager completion within 90s", latch.await(90, TimeUnit.SECONDS))
            assertEquals(enqueuedId, receivedId)
        } finally {
            context.unregisterReceiver(receiver)
            server.close()
        }
    }
}

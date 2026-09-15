package io.github.lord1egypt.prootx

import android.app.Service
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.* // ktlint-disable no-wildcard-imports
import io.github.lord1egypt.prootx.model.entities.App
import io.github.lord1egypt.prootx.model.entities.ServiceType
import io.github.lord1egypt.prootx.model.repositories.ProotXDatabase
import io.github.lord1egypt.prootx.model.entities.Session
import io.github.lord1egypt.prootx.utils.* // ktlint-disable no-wildcard-imports
import kotlin.coroutines.CoroutineContext

class ServerService : Service(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    companion object {
        const val SERVER_SERVICE_RESULT: String = "io.github.lord1egypt.prootx.ServerService.RESULT"
    }

    private val activeSessions: MutableMap<Long, Session> = mutableMapOf()

    private lateinit var broadcaster: LocalBroadcastManager

    private val notificationManager: NotificationConstructor by lazy {
        NotificationConstructor(this)
    }

    private val busyboxExecutor by lazy {
        val prootxFiles = ProotXFiles(this, this.applicationInfo.nativeLibraryDir)
        val prootDebugLogger = ProotDebugLogger(this.defaultSharedPreferences, prootxFiles)
        BusyboxExecutor(prootxFiles, prootDebugLogger)
    }

    private val localServerManager by lazy {
        LocalServerManager(this.filesDir.path, busyboxExecutor)
    }

    override fun onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this)
        notificationManager.createServiceNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.getStringExtra("type")) {
            "start" -> {
                val session: Session = intent.getParcelableExtra("session")!!
                promoteToForeground()
                this.launch { startSession(session) }
            }
            "stopApp" -> {
                val app: App = intent.getParcelableExtra("app")!!
                stopApp(app)
            }
            "restartRunningSession" -> {
                val session: Session = intent.getParcelableExtra("session")!!
                startClient(session)
            }
            "kill" -> {
                val session: Session = intent.getParcelableExtra("session")!!
                killSession(session)
            }
            "filesystemIsBeingDeleted" -> {
                val filesystemId: Long = intent.getLongExtra("filesystemId", -1)
                cleanUpFilesystem(filesystemId)
            }
            "stopAll" -> {
                activeSessions.forEach { (_, session) ->
                    killSession(session)
                }
            }
        }

        return START_STICKY
    }

    // Used in conjunction with manifest attribute `android:stopWithTask="true"`
    // to clean up when app is swiped away.
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Redundancy to ensure no hanging processes, given broad device spectrum.
        this.coroutineContext.cancel()
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Redundancy to ensure no hanging processes, given broad device spectrum.
        this.coroutineContext.cancel()
    }

    private fun removeSession(session: Session) {
        activeSessions.remove(session.pid)
        if (activeSessions.isEmpty()) {
            stopForeground(true)
            stopSelf()
        }
    }

    private fun updateSession(session: Session) = CoroutineScope(Dispatchers.Default).launch {
        ProotXDatabase.getInstance(this@ServerService).sessionDao().updateSession(session)
    }

    private fun killSession(session: Session) {
        localServerManager.stopService(session)
        removeSession(session)
        session.active = false
        updateSession(session)
    }

    // Promotes the service to the foreground synchronously, before any asynchronous
    // session-start work is scheduled, so the Android 8+ startForegroundService() deadline
    // is always met. The manifest declares the specialUse foreground service type; the
    // runtime pass-through constant keeps the manifest as the single source of truth.
    private fun promoteToForeground() {
        val notification = notificationManager.buildPersistentServiceNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationConstructor.serviceNotificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
            )
        } else {
            startForeground(NotificationConstructor.serviceNotificationId, notification)
        }
    }

    private suspend fun startSession(session: Session) {
        session.pid = localServerManager.startServer(session)

        while (!localServerManager.isServerRunning(session)) {
            delay(500)
        }

        session.active = true
        updateSession(session)
        startClient(session)
        activeSessions[session.pid] = session
    }

    private fun stopApp(app: App) {
        val appSessions = activeSessions.filter { (_, session) ->
            session.name == app.name
        }
        appSessions.forEach { (_, session) ->
            killSession(session)
        }
    }

    private fun startClient(session: Session) {
        when (session.serviceType) {
            ServiceType.Ssh -> startSshClient(session)
            ServiceType.Vnc -> startVncClient(session, "com.iiordanov.freebVNC")
            ServiceType.Xsdl -> startXsdlClient("x.org.server")
            else -> sendDialogBroadcast("unhandledSessionServiceType")
        }
        sendSessionActivatedBroadcast()
    }

    private fun startSshClient(session: Session) {
        val connectBotIntent = Intent()
        connectBotIntent.action = Intent.ACTION_VIEW
        connectBotIntent.data = Uri.parse("ssh://${session.username}@localhost:2022/#prootx")
        connectBotIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        startActivity(connectBotIntent)
    }

    private fun startVncClient(session: Session, packageName: String) {
        val bVncIntent = Intent()
        bVncIntent.action = Intent.ACTION_VIEW
        bVncIntent.type = "application/vnd.vnc"
        bVncIntent.data = Uri.parse("vnc://127.0.0.1:5951/?VncUsername=${session.username}&VncPassword=${session.vncPassword}")
        bVncIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        if (clientIsPresent(bVncIntent)) {
            this.startActivity(bVncIntent)
        } else {
            getClient(packageName)
        }
    }

    private fun startXsdlClient(packageName: String) {
        val xsdlIntent = Intent()
        xsdlIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        xsdlIntent.data = Uri.parse("x11://give.me.display:4721")

        if (clientIsPresent(xsdlIntent)) {
            startActivity(xsdlIntent)
        } else {
            getClient(packageName)
        }
    }

    private fun clientIsPresent(intent: Intent): Boolean {
        val activities = packageManager.queryIntentActivities(intent, 0)
        return (activities.size > 0)
    }

    private fun getClient(packageName: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            this.startActivity(intent)
        } catch (err: ActivityNotFoundException) {
            sendDialogBroadcast("playStoreMissingForClient")
        }
    }

    private fun cleanUpFilesystem(filesystemId: Long) {
        activeSessions.values.filter { it.filesystemId == filesystemId }
                .forEach { killSession(it) }
    }

    private fun sendSessionActivatedBroadcast() {
        val intent = Intent(SERVER_SERVICE_RESULT)
                .putExtra("type", "sessionActivated")
        broadcaster.sendBroadcast(intent)
    }

    private fun sendDialogBroadcast(type: String) {
        val intent = Intent(SERVER_SERVICE_RESULT)
                .putExtra("type", "dialog")
                .putExtra("dialogType", type)
        broadcaster.sendBroadcast(intent)
    }
}
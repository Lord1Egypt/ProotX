package io.github.lord1egypt.prootx

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.app.ForegroundServiceStartNotAllowedException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.* // ktlint-disable no-wildcard-imports
import com.google.android.material.textfield.TextInputEditText
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.appcompat.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.gson.Gson
import io.github.lord1egypt.prootx.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.github.lord1egypt.prootx.model.entities.App
import io.github.lord1egypt.prootx.model.entities.ServiceType
import io.github.lord1egypt.prootx.model.entities.Session
import io.github.lord1egypt.prootx.model.remote.GithubApiClient
import io.github.lord1egypt.prootx.model.repositories.AssetRepository
import io.github.lord1egypt.prootx.model.repositories.ProotXDatabase
import io.github.lord1egypt.prootx.model.state.* // ktlint-disable no-wildcard-imports
import io.github.lord1egypt.prootx.ui.AppsListFragment
import io.github.lord1egypt.prootx.ui.SessionListFragment
import io.github.lord1egypt.prootx.utils.* // ktlint-disable no-wildcard-imports
import io.github.lord1egypt.prootx.viewmodel.* // ktlint-disable no-wildcard-imports
import io.github.lord1egypt.prootx.ui.FilesystemListFragment
import io.github.lord1egypt.prootx.model.repositories.DownloadMetadata
import io.github.lord1egypt.prootx.utils.preferences.* // ktlint-disable no-wildcard-imports

class MainActivity : AppCompatActivity(), SessionListFragment.SessionSelection, AppsListFragment.AppSelection, FilesystemListFragment.FilesystemListProgress {

    companion object {
        // Shared one-time notification-permission prompt state. Both MainActivity and the
        // terminal activity (a separate module) must agree on these exact names.
        const val NOTIFICATION_PERMISSION_PREFS = "notification_permission"
        const val NOTIFICATION_PERMISSION_PROMPT_COMPLETED_KEY = "prompt_completed"
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 11
    }

    val className = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    private var progressBarIsVisible = false
    private var currentFragmentDisplaysProgressDialog = false
    private var autoStarted = false

    // The session that has been requested but not yet handed to ServerService, e.g. while the
    // notification-permission dialog is open or while the activity is not eligible to start a
    // foreground service.
    private var pendingSession: Session? = null
    private var notificationPermissionRequestInFlight = false

    private val logger = SentryLogger()
    private val prootxFiles by lazy { ProotXFiles(this, this.applicationInfo.nativeLibraryDir) }
    private val busyboxExecutor by lazy {
        val prootDebugLogger = ProotDebugLogger(this.defaultSharedPreferences, prootxFiles)
        BusyboxExecutor(prootxFiles, prootDebugLogger)
    }

    private val navController: NavController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

    private val notificationPermissionPreferences by lazy {
        getSharedPreferences(NOTIFICATION_PERMISSION_PREFS, Context.MODE_PRIVATE)
    }

    private val userFeedbackPrompter by lazy {
        UserFeedbackPrompter(this, findViewById(R.id.layout_user_prompt_insert))
    }

    private val optInPrompter by lazy {
        CollectionOptInPrompter(this, findViewById(R.id.layout_user_prompt_insert))
    }

    val billingManager by lazy {
        BillingManager(this, contributionPrompter.onEntitledSubPurchases, contributionPrompter.onEntitledInAppPurchases, contributionPrompter.onPurchase, contributionPrompter.onSubscriptionSupportedChecked)
    }

    private val contributionPrompter by lazy {
        ContributionPrompter(this, findViewById(R.id.layout_user_prompt_insert))
    }

    private val downloadBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == -1L) return
            else viewModel.submitCompletedDownloadId(id)
        }
    }

    private val serverServiceBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.getStringExtra("type")?.let { intentType ->
                val breadcrumb = ProotXBreadcrumb(className, BreadcrumbType.ReceivedIntent, intentType)
                logger.addBreadcrumb(breadcrumb)
                when (intentType) {
                    "sessionActivated" -> handleSessionHasBeenActivated()
                    "dialog" -> {
                        val type = intent.getStringExtra("dialogType") ?: ""
                        showDialog(type)
                    }
                }
            }
        }
    }

    private val stateObserver = Observer<State> {
        val breadcrumb = ProotXBreadcrumb(className, BreadcrumbType.ObservedState, "$it")
        logger.addBreadcrumb(breadcrumb)
        it?.let { state ->
            handleStateUpdate(state)
        }
    }

    private val viewModel: MainActivityViewModel by lazy {
        val ulaDatabase = ProotXDatabase.getInstance(this)

        val assetPreferences = AssetPreferences(this)
        val githubApiClient = GithubApiClient(prootxFiles)
        val assetRepository = AssetRepository(filesDir.path, assetPreferences, githubApiClient)

        val filesystemManager = FilesystemManager(prootxFiles, busyboxExecutor)
        val storageCalculator = StorageCalculator(StatFs(filesDir.path))

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadManagerWrapper = DownloadManagerWrapper(downloadManager)
        val assetDownloader = AssetDownloader(assetPreferences, downloadManagerWrapper, prootxFiles)

        val appsStartupFsm = AppsStartupFsm(ulaDatabase, filesystemManager, prootxFiles)
        val sessionStartupFsm = SessionStartupFsm(ulaDatabase, assetRepository, filesystemManager, assetDownloader, storageCalculator)
        ViewModelProvider(this, MainActivityViewModelFactory(appsStartupFsm, sessionStartupFsm))
                .get(MainActivityViewModel::class.java)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.type.equals("settings"))
            navController.navigate(R.id.settings_fragment)
        else
            autoStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setNavStartDestination()
        setProgressDialogNavListeners()

        setupWithNavController(binding.bottomNavView, navController)

        val promptViewHolder = findViewById<ViewGroup>(R.id.layout_user_prompt_insert)
        if (userFeedbackPrompter.viewShouldBeShown()) {
            userFeedbackPrompter.showView()
        }

        if (optInPrompter.viewShouldBeShown()) {
            optInPrompter.showView()
        }

        if (contributionPrompter.viewShouldBeShown()) {
            contributionPrompter.showView()
        }

        handleQWarning()

        if (optInPrompter.userHasOptedIn()) {
            logger.initialize(this)
        }

        viewModel.getState().observe(this, stateObserver)
        if (intent?.type.equals("settings"))
            navController.navigate(R.id.settings_fragment)
        else
            autoStart()
    }

    private fun setNavStartDestination() {
        val userPreference = defaultSharedPreferences.getString("pref_default_nav_location", "Apps")
        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        graph.startDestination = when (userPreference) {
            getString(R.string.sessions) -> R.id.session_list_fragment
            else -> R.id.app_list_fragment
        }
        navController.graph = graph
    }

    private fun setProgressDialogNavListeners() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentFragmentDisplaysProgressDialog =
                    destination.label == getString(R.string.sessions) ||
                            destination.label == getString(R.string.apps) ||
                            destination.label == getString(R.string.filesystems)
            if (!currentFragmentDisplaysProgressDialog) killProgressBar()
            else if (progressBarIsVisible) displayProgressBar()
        }
    }

    private fun handleQWarning() {
        val handler = QWarningHandler(this.getSharedPreferences(QWarningHandler.prefsString, Context.MODE_PRIVATE), prootxFiles)
        if (handler.messageShouldBeDisplayed()) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.q_warning_title)
                    .setMessage(R.string.q_warning_message)
                    .setPositiveButton(R.string.button_ok) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setNeutralButton(R.string.wiki) {
                        dialog, _ ->
                        dialog.dismiss()
                        sendWikiIntent()
                    }
                    .create().show()
            handler.messageHasBeenDisplayed()
        }
    }

    override fun onSupportNavigateUp() = navController.navigateUp()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)
        return true
    }

    private fun autoStart() {
        val prefs = getSharedPreferences("apps", Context.MODE_PRIVATE)
        val json = prefs.getString("AutoApp", " ")
        if (json != null)
            if (json.compareTo(" ") != 0) {
                val gson = Gson()
                val autoApp = gson.fromJson(json, App::class.java)
                autoStarted = true
                appHasBeenSelected(autoApp, true)
            }
    }

    // DownloadManager.ACTION_DOWNLOAD_COMPLETE is a system broadcast. Android 14 exempts
    // receivers registered only for system broadcasts from the exported/not-exported flag
    // requirement, so the flag-less platform registration is intentional; the lint check is a
    // known false positive for this case.
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(serverServiceBroadcastReceiver, IntentFilter(ServerService.SERVER_SERVICE_RESULT))
        registerReceiver(downloadBroadcastReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onResume() {
        super.onResume()
        billingManager.querySubPurchases()
        billingManager.queryInAppPurchases()
        viewModel.handleOnResume()
        // A session may have been deferred while the notification-permission dialog was open or
        // while the activity was not eligible to start a foreground service.
        continuePendingSessionIfPossible()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != NOTIFICATION_PERMISSION_REQUEST_CODE) return

        notificationPermissionRequestInFlight = false
        // grantResults is empty when the dialog was dismissed without an explicit decision; only
        // an explicit allow/deny result is recorded as a completed prompt.
        if (grantResults.isNotEmpty()) {
            notificationPermissionPreferences.edit()
                    .putBoolean(NOTIFICATION_PERMISSION_PROMPT_COMPLETED_KEY, true)
                    .apply()
        }
        // Notification permission never blocks the Linux session.
        continuePendingSessionIfPossible()
    }

    override fun onDestroy() {
        billingManager.destroy()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.terms_and_conditions) {
            val intent = Intent("android.intent.action.VIEW", Uri.parse("https://github.com/Lord1Egypt/ProotX/blob/main/LICENSE"))
            startActivity(intent)
        }
        if (item.itemId == R.id.option_wiki) {
            sendWikiIntent()
        }
        if (item.itemId == R.id.clear_support_files) {
            displayClearSupportFilesDialog()
        }
        return NavigationUI.onNavDestinationSelected(item,
                Navigation.findNavController(this, R.id.nav_host_fragment)) ||
                super.onOptionsItemSelected(item)
    }

    private fun sendWikiIntent() {
        val intent = Intent("android.intent.action.VIEW", Uri.parse("https://github.com/Lord1Egypt/ProotX"))
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()

        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(serverServiceBroadcastReceiver)
        unregisterReceiver(downloadBroadcastReceiver)
    }

    override fun appHasBeenSelected(app: App, autoStart: Boolean) {
        viewModel.submitAppSelection(app, autoStart)
    }

    override fun sessionHasBeenSelected(session: Session) {
        viewModel.submitSessionSelection(session)
    }

    private fun handleStateUpdate(newState: State) {
        return when (newState) {
            is WaitingForInput -> { killProgressBar() }
            is CanOnlyStartSingleSession -> {
                showToast(R.string.single_session_supported)
                viewModel.handleUserInputCancelled()
            }
            is SessionCanBeStarted -> { prepareSessionForStart(newState.session) }
            is SessionCanBeRestarted -> { restartRunningSession(newState.session) }
            is IllegalState -> { handleIllegalState(newState) }
            is UserInputRequiredState -> { handleUserInputState(newState) }
            is ProgressBarUpdateState -> { handleProgressBarUpdateState(newState) }
        }
    }

    private fun prepareSessionForStart(session: Session) {
        val step = getString(R.string.progress_starting)
        val details = ""
        updateProgressBar(step, details)

        // TODO: Alert user when defaulting to VNC
        // TODO: Is this even possible?
        if (session.serviceType is ServiceType.Xsdl && Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            session.serviceType = ServiceType.Vnc
        }

        when (session.serviceType) {
            ServiceType.Xsdl -> {
                viewModel.lastSelectedSession = session
                sendXsdlIntentToSetDisplayNumberAndExpectResult()
            }
            ServiceType.Vnc -> {
                setVncResolution(session)
                startSession(session)
            }
            else -> startSession(session)
        }
    }

    private fun setVncResolution(session: Session) {
        val deviceDimensions = DeviceDimensions()
        val windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val orientation = applicationContext.resources.configuration.orientation
        deviceDimensions.saveDeviceDimensions(windowManager, DisplayMetrics(), orientation, defaultSharedPreferences)
        session.geometry = deviceDimensions.getScreenResolution()
    }

    private fun startSession(session: Session) {
        pendingSession = session
        continuePendingSessionIfPossible()
    }

    private fun notificationPermissionIsGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private fun notificationPermissionPromptHasCompleted(): Boolean =
            notificationPermissionPreferences.getBoolean(NOTIFICATION_PERMISSION_PROMPT_COMPLETED_KEY, false)

    private fun shouldRequestNotificationPermission(): Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !notificationPermissionIsGranted() &&
                    !notificationPermissionPromptHasCompleted()

    private fun activityIsEligibleToStartForegroundService(): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                    lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)

    // Final session-start boundary: gate on foreground eligibility, present the one-time
    // notification-permission request in context, then launch ServerService regardless of the
    // permission outcome.
    private fun continuePendingSessionIfPossible() {
        val session = pendingSession ?: return
        if (!activityIsEligibleToStartForegroundService()) return
        if (notificationPermissionRequestInFlight) return

        if (shouldRequestNotificationPermission()) {
            notificationPermissionRequestInFlight = true
            requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        pendingSession = null
        launchServerServiceForSession(session)
    }

    private fun launchServerServiceForSession(session: Session) {
        val serviceIntent = Intent(this, ServerService::class.java)
                .putExtra("type", "start")
                .putExtra("session", session)
        if (!launchForegroundService(serviceIntent)) {
            // The foreground-service start raced with a background transition; keep the
            // user-requested session pending and retry on the next resume.
            pendingSession = session
            val breadcrumb = ProotXBreadcrumb(
                    className,
                    BreadcrumbType.RuntimeError,
                    "ForegroundServiceStartNotAllowedException; session start deferred until foreground"
            )
            logger.addBreadcrumb(breadcrumb)
            return
        }
        if (autoStarted) {
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 2000)
        }
    }

    private fun launchForegroundService(serviceIntent: Intent): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                startForegroundService(serviceIntent)
                true
            } catch (e: ForegroundServiceStartNotAllowedException) {
                false
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
            true
        } else {
            startService(serviceIntent)
            true
        }
    }

    /*
    XSDL has a different flow than starting SSH/VNC session.  It sends an intent to XSDL with
        with a display value.  Then XSDL sends an intent to open ProotX signalling
        that it has an xserver listening.  We set the initial display number as an environment variable
        then start a twm process to connect to XSDL's xserver.
    */
    private fun sendXsdlIntentToSetDisplayNumberAndExpectResult() {
        try {
            val xsdlIntent = Intent(Intent.ACTION_MAIN, Uri.parse("x11://give.me.display:4721"))
            val setDisplayRequestCode = 1
            startActivityForResult(xsdlIntent, setDisplayRequestCode)
        } catch (e: Exception) {
            val appPackageName = "x.org.server"
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (error: android.content.ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            val session = viewModel.lastSelectedSession
            val result = data.getStringExtra("run") ?: ""
            if (session.serviceType == ServiceType.Xsdl && result.isNotEmpty()) {
                startSession(session)
            }
        }
    }

    private fun restartRunningSession(session: Session) {
        val serviceIntent = Intent(this, ServerService::class.java)
                .putExtra("type", "restartRunningSession")
                .putExtra("session", session)
        startService(serviceIntent)
    }

    private fun handleSessionHasBeenActivated() {
        viewModel.handleSessionHasBeenActivated()
        killProgressBar()
    }

    private fun showToast(resId: Int) {
        val content = getString(resId)
        Toast.makeText(this, content, Toast.LENGTH_LONG).show()
    }

    private fun handleUserInputState(state: UserInputRequiredState) {
        return when (state) {
            is LowStorageAcknowledgementRequired -> {
                displayLowStorageDialog()
            }
            is FilesystemCredentialsRequired -> {
                getCredentials()
            }
            is AppServiceTypePreferenceRequired -> {
                getServiceTypePreference()
            }
            is LargeDownloadRequired -> {
                if (wifiIsEnabled()) {
                    viewModel.startAssetDownloads(state.downloadRequirements)
                    return
                }
                displayNetworkChoicesDialog(state.downloadRequirements)
            }
            is ActiveSessionsMustBeDeactivated -> {
                displayGenericErrorDialog(R.string.general_error_title, R.string.deactivate_sessions)
            }
        }
    }

    private fun handleIllegalState(state: IllegalState) {
        val stateDescription = IllegalStateHandler.getLocalizationData(state).getString(this)
        val displayMessage = getString(R.string.illegal_state_github_message, stateDescription)

        AlertDialog.Builder(this)
                .setMessage(displayMessage)
                .setTitle(R.string.illegal_state_title)
                .setPositiveButton(R.string.button_ok) {
                    dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
    }

    // TODO sealed classes?
    private fun showDialog(dialogType: String) {
        when (dialogType) {
            "unhandledSessionServiceType" -> {
                displayGenericErrorDialog(R.string.general_error_title,
                        R.string.illegal_state_unhandled_session_service_type)
            }
            "playStoreMissingForClient" ->
                displayGenericErrorDialog(R.string.alert_need_client_app_title,
                    R.string.alert_need_client_app_message)
        }
    }

    private fun displayClearSupportFilesDialog() {
        AlertDialog.Builder(this)
                .setMessage(R.string.alert_clear_support_files_message)
                .setTitle(R.string.alert_clear_support_files_title)
                .setPositiveButton(R.string.alert_clear_support_files_clear_button) { dialog, _ ->
                    handleClearSupportFiles()
                    dialog.dismiss()
                }
                .setNeutralButton(R.string.button_cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
    }

    private fun handleClearSupportFiles() {
        val appsPreferences = AppsPreferences(this)
        val assetDirectoryNames = appsPreferences.getDistributionsList().plus("support")
        val assetFileClearer = AssetFileClearer(prootxFiles, assetDirectoryNames, busyboxExecutor)
        CoroutineScope(Dispatchers.Main).launch { viewModel.handleClearSupportFiles(assetFileClearer) }
    }

    private fun handleProgressBarUpdateState(state: ProgressBarUpdateState) {
        return when (state) {
            is StartingSetup -> {
                val step = getString(R.string.progress_start_step)
                updateProgressBar(step, "")
            }
            is FetchingAssetLists -> {
                val step = getString(R.string.progress_fetching_asset_lists)
                updateProgressBar(step, "")
            }
            is CheckingForAssetsUpdates -> {
                val step = getString(R.string.progress_checking_for_required_updates)
                updateProgressBar(step, "")
            }
            is DownloadProgress -> {
                val step = getString(R.string.progress_downloading)
                val details = getString(R.string.progress_downloading_out_of, state.numComplete, state.numTotal)
                updateProgressBar(step, details)
            }
            is CopyingDownloads -> {
                val step = getString(R.string.progress_copying_downloads)
                updateProgressBar(step, "")
            }
            is VerifyingFilesystem -> {
                val step = getString(R.string.progress_verifying_assets)
                updateProgressBar(step, "")
            }
            is VerifyingAvailableStorage -> {
                val step = getString(R.string.progress_verifying_sufficient_storage)
                updateProgressBar(step, "")
            }
            is FilesystemExtractionStep -> {
                val step = getString(R.string.progress_setting_up_filesystem)
                val details = getString(R.string.progress_extraction_details, state.extractionTarget)
                updateProgressBar(step, details)
            }
            is ClearingSupportFiles -> {
                val step = getString(R.string.progress_clearing_support_files)
                updateProgressBar(step, "")
            }
            is ProgressBarOperationComplete -> {
                killProgressBar()
            }
        }
    }

    override fun updateFilesystemExportProgress(details: String) {
        val step = getString(R.string.progress_exporting_filesystem)
        updateProgressBar(step, details)
    }

    override fun updateFilesystemDeleteProgress() {
        val step = getString(R.string.progress_deleting_filesystem)
        updateProgressBar(step, "")
    }

    override fun stopProgressFromFilesystemList() {
        killProgressBar()
    }

    private fun displayProgressBar() {
        if (!currentFragmentDisplaysProgressDialog) return

        if (!progressBarIsVisible) {
            val inAnimation = AlphaAnimation(0f, 1f)
            inAnimation.duration = 200
            binding.layoutProgress.animation = inAnimation

            binding.layoutProgress.visibility = View.VISIBLE
            binding.layoutProgress.isFocusable = true
            binding.layoutProgress.isClickable = true
            progressBarIsVisible = true
        }
    }

    private fun updateProgressBar(step: String, details: String) {
        displayProgressBar()

        binding.textSessionListProgressStep.text = step
        binding.textSessionListProgressDetails.text = details
    }

    private fun killProgressBar() {
        val outAnimation = AlphaAnimation(1f, 0f)
        outAnimation.duration = 200
        binding.layoutProgress.animation = outAnimation
        binding.layoutProgress.visibility = View.GONE
        binding.layoutProgress.isFocusable = false
        binding.layoutProgress.isClickable = false
        progressBarIsVisible = false
    }

    private fun wifiIsEnabled(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        for (network in connectivityManager.allNetworks) {
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) return true
        }
        return false
    }

    private fun displayNetworkChoicesDialog(downloadsToContinue: List<DownloadMetadata>) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.alert_wifi_disabled_message)
                .setTitle(R.string.alert_wifi_disabled_title)
                .setPositiveButton(R.string.alert_wifi_disabled_continue_button) {
                    dialog, _ ->
                    dialog.dismiss()
                    viewModel.startAssetDownloads(downloadsToContinue)
                }
                .setNegativeButton(R.string.alert_wifi_disabled_turn_on_wifi_button) {
                    dialog, _ ->
                    dialog.dismiss()
                    startActivity(Intent(WifiManager.ACTION_PICK_WIFI_NETWORK))
                    viewModel.handleUserInputCancelled()
                    killProgressBar()
                }
                .setNeutralButton(R.string.alert_wifi_disabled_cancel_button) {
                    dialog, _ ->
                    dialog.dismiss()
                    viewModel.handleUserInputCancelled()
                    killProgressBar()
                }
                .setOnCancelListener {
                    viewModel.handleUserInputCancelled()
                    killProgressBar()
                }
                .create()
                .show()
    }

    private fun getCredentials() {
        val dialog = AlertDialog.Builder(this)
        val dialogView = this.layoutInflater.inflate(R.layout.dia_app_credentials, null)
        dialog.setView(dialogView)
        dialog.setCancelable(true)
        dialog.setPositiveButton(R.string.button_continue, null)
        val customDialog = dialog.create()

        customDialog.setOnShowListener {
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val username = customDialog.find<TextInputEditText>(R.id.text_input_username).text.toString()
                val password = customDialog.find<TextInputEditText>(R.id.text_input_password).text.toString()
                val vncPassword = customDialog.find<TextInputEditText>(R.id.text_input_vnc_password).text.toString()

                if (validateCredentials(username, password, vncPassword)) {
                    customDialog.dismiss()
                    viewModel.submitFilesystemCredentials(username, password, vncPassword)
                }
            }
        }
        customDialog.setOnCancelListener {
            viewModel.handleUserInputCancelled()
        }
        customDialog.show()
    }

    private fun displayLowStorageDialog() {
        displayGenericErrorDialog(R.string.alert_storage_low_title, R.string.alert_storage_low_message) {
            viewModel.lowAvailableStorageAcknowledged()
        }
    }

    // TODO refactor the names here
    // TODO could this dialog share a layout with the apps details page somehow?
    private fun getServiceTypePreference() {
        val dialog = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dia_app_select_client, null)
        dialog.setView(dialogView)
        dialog.setCancelable(true)
        dialog.setPositiveButton(R.string.button_continue, null)
        val customDialog = dialog.create()

        customDialog.setOnShowListener {
            val sshTypePreference = customDialog.find<RadioButton>(R.id.ssh_radio_button)
            val vncTypePreference = customDialog.find<RadioButton>(R.id.vnc_radio_button)
            val xsdlTypePreference = customDialog.find<RadioButton>(R.id.xsdl_radio_button)

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                xsdlTypePreference.isEnabled = false
                xsdlTypePreference.alpha = 0.5f

                val xsdlSupportedText = customDialog.findViewById<TextView>(R.id.text_xsdl_version_supported_description)
                xsdlSupportedText.visibility = View.VISIBLE
            }

            if (!viewModel.lastSelectedApp.supportsCli) {
                sshTypePreference.isEnabled = false
                sshTypePreference.alpha = 0.5f
            }

            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                customDialog.dismiss()
                val selectedType = when {
                    sshTypePreference.isChecked -> ServiceType.Ssh
                    vncTypePreference.isChecked -> ServiceType.Vnc
                    xsdlTypePreference.isChecked -> ServiceType.Xsdl
                    else -> ServiceType.Unselected
                }
                viewModel.submitAppServiceType(selectedType)
            }
        }
        customDialog.setOnCancelListener {
            viewModel.handleUserInputCancelled()
        }

        customDialog.show()
    }

    private fun validateCredentials(username: String, password: String, vncPassword: String): Boolean {
        val blacklistedUsernames = this.resources.getStringArray(R.array.blacklisted_usernames)
        val validator = CredentialValidator()

        val usernameCredentials = validator.validateUsername(username, blacklistedUsernames)
        val passwordCredentials = validator.validatePassword(password)
        val vncPasswordCredentials = validator.validateVncPassword(vncPassword)

        return when {
            !usernameCredentials.credentialIsValid -> {
                Toast.makeText(this, usernameCredentials.errorMessageId, Toast.LENGTH_LONG).show()
                false
            }
            !passwordCredentials.credentialIsValid -> {
                Toast.makeText(this, passwordCredentials.errorMessageId, Toast.LENGTH_LONG).show()
                false
            }
            !vncPasswordCredentials.credentialIsValid -> {
                Toast.makeText(this, vncPasswordCredentials.errorMessageId, Toast.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }
}
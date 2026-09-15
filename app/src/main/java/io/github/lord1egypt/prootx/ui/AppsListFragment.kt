package io.github.lord1egypt.prootx.ui

import android.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.view.MenuInflater
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import io.github.lord1egypt.prootx.databinding.FragAppListBinding
import io.github.lord1egypt.prootx.MainActivity
import io.github.lord1egypt.prootx.R
import io.github.lord1egypt.prootx.ServerService
import io.github.lord1egypt.prootx.model.entities.App
import io.github.lord1egypt.prootx.model.remote.GithubAppsFetcher
import io.github.lord1egypt.prootx.model.repositories.AppsRepository
import io.github.lord1egypt.prootx.model.repositories.RefreshStatus
import io.github.lord1egypt.prootx.model.repositories.ProotXDatabase
import io.github.lord1egypt.prootx.utils.* // ktlint-disable no-wildcard-imports
import io.github.lord1egypt.prootx.utils.preferences.AppsPreferences
import io.github.lord1egypt.prootx.viewmodel.AppsListViewModel
import io.github.lord1egypt.prootx.viewmodel.AppsListViewModelFactory

class AppsListFragment : Fragment(), AppsListAdapter.AppsClickHandler {

    private var _binding: FragAppListBinding? = null
    private val binding get() = _binding!!

    interface AppSelection {
        fun appHasBeenSelected(app: App, autoStart: Boolean)
    }

    private val doOnAppSelection: AppSelection by lazy {
        activityContext
    }

    private lateinit var activityContext: MainActivity

    private val appsAdapter by lazy {
        AppsListAdapter(activityContext, this)
    }

    private var refreshStatus = RefreshStatus.INACTIVE

    private val appsPreferences by lazy {
        AppsPreferences(activityContext)
    }

    private val viewModel: AppsListViewModel by lazy {
        val ulaDatabase = ProotXDatabase.getInstance(activityContext)
        val appsDao = ulaDatabase.appsDao()
        val githubFetcher = GithubAppsFetcher("${activityContext.filesDir}")

        val appsRepository = AppsRepository(appsDao, githubFetcher, appsPreferences)
        ViewModelProvider(this, AppsListViewModelFactory(appsRepository))
                .get(AppsListViewModel::class.java)
    }

    private val appsObserver = Observer<List<App>> {
        it?.let { list ->
            appsAdapter.updateApps(list)
            binding.listApps.scrollToPosition(0)
            if (list.isEmpty() || appIsNewVersion()) {
                doRefresh()
            }
        }
    }

    private val activeAppsObserver = Observer<List<App>> {
        it?.let { list ->
            appsAdapter.updateActiveApps(list)
        }
    }

    private val refreshStatusObserver = Observer<RefreshStatus> {
        it?.let { newStatus ->
            refreshStatus = newStatus
            binding.swipeRefresh.isRefreshing = refreshStatus == RefreshStatus.ACTIVE

            if (refreshStatus == RefreshStatus.FAILED) showRefreshUnavailableDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_refresh, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when {
            item.itemId == R.id.menu_item_refresh -> {
                binding.swipeRefresh.isRefreshing = true
                doRefresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(app: App) {
        doOnAppSelection.appHasBeenSelected(app, false)
    }

    override fun createContextMenu(menu: Menu) {
        activityContext.menuInflater.inflate(R.menu.context_menu_apps, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_app_details -> showAppDetails(appsAdapter.contextMenuItem)
            R.id.menu_item_stop_app -> stopAppSession(appsAdapter.contextMenuItem)
            else -> super.onContextItemSelected(item)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragAppListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activityContext = activity!! as MainActivity
        viewModel.getAppsList().observe(viewLifecycleOwner, appsObserver)
        viewModel.getActiveApps().observe(viewLifecycleOwner, activeAppsObserver)
        viewModel.getRefreshStatus().observe(viewLifecycleOwner, refreshStatusObserver)

        registerForContextMenu(binding.listApps)
        binding.listApps.layoutManager = LinearLayoutManager(binding.listApps.context)
        binding.listApps.adapter = appsAdapter

        binding.swipeRefresh.setOnRefreshListener { doRefresh() }
        binding.swipeRefresh.setColorSchemeResources(
                R.color.holo_blue_light,
                R.color.holo_green_light,
                R.color.holo_orange_light,
                R.color.holo_red_light)
    }

    private fun doRefresh() {
        viewModel.refreshAppsList()
        setLatestUpdateProotXVersion()
    }

    private fun showAppDetails(app: App): Boolean {
        val bundle = bundleOf("app" to app)
        this.findNavController().navigate(R.id.action_app_list_to_app_details, bundle)
        return true
    }

    private fun stopAppSession(app: App): Boolean {
        val serviceIntent = Intent(activityContext, ServerService::class.java)
                .putExtra("type", "stopApp")
                .putExtra("app", app)
        activityContext.startService(serviceIntent)
        return true
    }

    private fun showRefreshUnavailableDialog() {
        AlertDialog.Builder(activityContext)
                .setMessage(R.string.alert_network_required_for_refresh)
                .setTitle(R.string.general_error_title)
                .setPositiveButton(R.string.button_ok) {
                    dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
    }

    private fun appIsNewVersion(): Boolean {
        val version = getProotXVersion()
        val lastUpdatedVersion = activityContext.defaultSharedPreferences.getString("lastAppsUpdate", "")
        return version != lastUpdatedVersion
    }

    private fun setLatestUpdateProotXVersion() {
        val version = getProotXVersion()
        with(activityContext.defaultSharedPreferences.edit()) {
            putString("lastAppsUpdate", version)
            apply()
        }
    }

    private fun getProotXVersion(): String {
        val info = activityContext.packageManager.getPackageInfo(activityContext.packageName, 0)
        return info.versionName!!
    }
}
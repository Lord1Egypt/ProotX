package io.github.lord1egypt.prootx.model.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.* // ktlint-disable no-wildcard-imports
import io.github.lord1egypt.prootx.model.daos.AppsDao
import io.github.lord1egypt.prootx.model.entities.App
import io.github.lord1egypt.prootx.model.remote.GithubAppsFetcher
import io.github.lord1egypt.prootx.utils.BreadcrumbType
import io.github.lord1egypt.prootx.utils.Logger
import io.github.lord1egypt.prootx.utils.SentryLogger
import io.github.lord1egypt.prootx.utils.ProotXBreadcrumb
import io.github.lord1egypt.prootx.utils.preferences.AppsPreferences
import java.util.Locale

class AppsRepository(
    private val appsDao: AppsDao,
    private val remoteAppsSource: GithubAppsFetcher,
    private val appsPreferences: AppsPreferences,
    private val logger: Logger = SentryLogger()
) {
    private val className = "AppsRepository"

    private val refreshStatus = MutableLiveData<RefreshStatus>()

    fun getAllApps(): LiveData<List<App>> {
        return appsDao.getAllApps()
    }

    fun getActiveApps(): LiveData<List<App>> {
        return appsDao.getActiveApps()
    }

    fun getRefreshStatus(): LiveData<RefreshStatus> {
        return refreshStatus
    }

    suspend fun refreshData(scope: CoroutineScope) {
        val distributionsList = mutableSetOf<String>()
        refreshStatus.postValue(RefreshStatus.ACTIVE)
        val jobs = mutableListOf<Job>()
        try {
            remoteAppsSource.fetchAppsList().forEach { app ->
                jobs.add(scope.launch {
                    if (app.category.lowercase(Locale.ENGLISH) == "distribution") distributionsList.add(app.name)
                    remoteAppsSource.fetchAppIcon(app)
                    remoteAppsSource.fetchAppDescription(app)
                    remoteAppsSource.fetchAppScript(app)
                    appsDao.insertApp(app) // Insert the db element last to force observer refresh
            }) }
        } catch (err: Exception) {
            refreshStatus.postValue(RefreshStatus.FAILED)
            val message = err.message ?: "Not found"
            val breadcrumb = ProotXBreadcrumb(className, BreadcrumbType.RuntimeError, message)
            logger.addBreadcrumb(breadcrumb)
            logger.sendEvent("App Refresh Failed")
            return
        }
        jobs.joinAll()
        refreshStatus.postValue(RefreshStatus.FINISHED)
        appsPreferences.setDistributionsList(distributionsList)
    }
}

enum class RefreshStatus {
    ACTIVE, FINISHED, FAILED, INACTIVE
}

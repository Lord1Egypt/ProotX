package io.github.lord1egypt.prootx.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import io.github.lord1egypt.prootx.model.repositories.ProotXDatabase
import io.github.lord1egypt.prootx.model.entities.Filesystem
import io.github.lord1egypt.prootx.model.entities.Session
import io.github.lord1egypt.prootx.utils.* // ktlint-disable no-wildcard-imports

class SessionListViewModel(
    private val ulaDatabase: ProotXDatabase
) : ViewModel() {

    private val sessions: LiveData<List<Session>> by lazy {
        ulaDatabase.sessionDao().getAllSessions()
    }

    private val filesystems: LiveData<List<Filesystem>> by lazy {
        ulaDatabase.filesystemDao().getAllFilesystems()
    }

    fun getSessionsAndFilesystems(): LiveData<Pair<List<Session>, List<Filesystem>>> {
        return zipLiveData(sessions, filesystems)
    }

    fun deleteSessionById(id: Long) {
        GlobalScope.launch { ulaDatabase.sessionDao().deleteSessionById(id) }
    }
}

class SessionListViewModelFactory(private val ulaDatabase: ProotXDatabase) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SessionListViewModel(ulaDatabase) as T
    }
}
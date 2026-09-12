package io.github.lord1egypt.prootx.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import io.github.lord1egypt.prootx.model.daos.FilesystemDao
import io.github.lord1egypt.prootx.model.daos.SessionDao
import io.github.lord1egypt.prootx.model.entities.Filesystem
import io.github.lord1egypt.prootx.model.entities.Session
import io.github.lord1egypt.prootx.model.repositories.ProotXDatabase

@RunWith(MockitoJUnitRunner::class)
class SessionEditViewModelTest {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock lateinit var mockProotXDatabase: ProotXDatabase

    @Mock lateinit var mockFilesystemDao: FilesystemDao

    @Mock lateinit var mockSessionDao: SessionDao

    @Mock lateinit var mockObserver: Observer<List<Filesystem>>

    private lateinit var filesystemsLiveData: MutableLiveData<List<Filesystem>>

    private lateinit var sessionEditViewModel: SessionEditViewModel

    @Before
    fun setup() {
        filesystemsLiveData = MutableLiveData()
        whenever(mockProotXDatabase.filesystemDao()).thenReturn(mockFilesystemDao)
        whenever(mockFilesystemDao.getAllFilesystems()).thenReturn(filesystemsLiveData)
        whenever(mockProotXDatabase.sessionDao()).thenReturn(mockSessionDao)

        sessionEditViewModel = SessionEditViewModel(mockProotXDatabase)
    }

    @Test
    fun `Filesystems can be observed through getAllFilesystems`() {
        val filesystemsList = listOf(Filesystem(0))
        filesystemsLiveData.postValue(filesystemsList)

        sessionEditViewModel.getAllFilesystems().observeForever(mockObserver)

        verify(mockObserver).onChanged(filesystemsList)
    }

    @Test
    fun `Inserting a session propagates to the model layer`() {
        val session = Session(0, filesystemId = 0)

        runBlocking { sessionEditViewModel.insertSession(session, this) }

        verify(mockSessionDao).insertSession(session)
    }

    @Test
    fun `Updating a session propagates to the model layer`() {
        val session = Session(0, filesystemId = 0)

        runBlocking { sessionEditViewModel.updateSession(session, this) }

        verify(mockSessionDao).updateSession(session)
    }
}
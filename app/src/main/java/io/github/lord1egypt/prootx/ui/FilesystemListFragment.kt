package io.github.lord1egypt.prootx.ui

import android.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.* // ktlint-disable no-wildcard-imports
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import io.github.lord1egypt.prootx.databinding.FragFilesystemListBinding
import io.github.lord1egypt.prootx.MainActivity
import io.github.lord1egypt.prootx.R
import io.github.lord1egypt.prootx.ServerService
import io.github.lord1egypt.prootx.model.entities.Filesystem
import io.github.lord1egypt.prootx.model.repositories.ProotXDatabase
import io.github.lord1egypt.prootx.viewmodel.* // ktlint-disable no-wildcard-imports
import io.github.lord1egypt.prootx.model.entities.Session
import io.github.lord1egypt.prootx.utils.* // ktlint-disable no-wildcard-imports
import io.github.lord1egypt.prootx.viewmodel.FilesystemListViewModel

private const val FILESYSTEM_EXPORT_REQUEST_CODE = 7

class FilesystemListFragment : Fragment() {

    private var _binding: FragFilesystemListBinding? = null
    private val binding get() = _binding!!

    interface FilesystemListProgress {
        fun updateFilesystemExportProgress(details: String)
        fun updateFilesystemDeleteProgress()
        fun stopProgressFromFilesystemList()
    }

    private lateinit var activityContext: MainActivity

    private lateinit var filesystemList: List<Filesystem>

    private lateinit var activeSessions: List<Session>

    private val filesystemListViewModel: FilesystemListViewModel by lazy {
        val filesystemDao = ProotXDatabase.getInstance(activityContext).filesystemDao()
        val sessionDao = ProotXDatabase.getInstance(activityContext).sessionDao()

        val prootxFiles = ProotXFiles(activityContext, activityContext.applicationInfo.nativeLibraryDir)
        val prootDebugLogger = ProotDebugLogger(activityContext.defaultSharedPreferences, prootxFiles)
        val busyboxExecutor = BusyboxExecutor(prootxFiles, prootDebugLogger)

        val filesystemManager = FilesystemManager(prootxFiles, busyboxExecutor)
        ViewModelProvider(this, FilesystemListViewmodelFactory(filesystemDao, sessionDao, filesystemManager)).get(FilesystemListViewModel::class.java)
    }

    private val filesystemChangeObserver = Observer<List<Filesystem>> {
        it?.let { list ->
            filesystemList = list

            binding.listFilesystems.adapter = FilesystemListAdapter(activityContext, filesystemList)
        }
    }

    private val viewStateObserver = Observer<FilesystemListViewState> {
        it?.let { viewState ->
            when (viewState) {
                is FilesystemExportState -> handleExportStatus(viewState)
                is FilesystemDeleteState -> handleDeleteStatus(viewState)
            }
        }
    }

    private val activeSessionObserver = Observer<List<Session>> {
        it?.let { sessions -> activeSessions = sessions }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_create, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.menu_item_add) {
            editFilesystem(Filesystem(0))
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragFilesystemListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activityContext = activity!! as MainActivity
        filesystemListViewModel.getAllFilesystems().observe(viewLifecycleOwner, filesystemChangeObserver)
        filesystemListViewModel.getViewState().observe(viewLifecycleOwner, viewStateObserver)
        filesystemListViewModel.getAllActiveSessions().observe(viewLifecycleOwner, activeSessionObserver)
        registerForContextMenu(binding.listFilesystems)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        activityContext.menuInflater.inflate(R.menu.context_menu_filesystems, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val menuInfo = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val position = menuInfo.position
        val filesystem = filesystemList[position]
        when (item.itemId) {
            R.id.menu_item_filesystem_edit -> editFilesystem(filesystem)
            R.id.menu_item_filesystem_delete -> deleteFilesystem(filesystem)
            R.id.menu_item_filesystem_export -> exportFilesystem(filesystem)
            else -> super.onContextItemSelected(item)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILESYSTEM_EXPORT_REQUEST_CODE) {
            data?.data?.let { uri ->
                val filesDir = activityContext.filesDir
                filesystemListViewModel.startExport(filesDir, uri, activityContext.contentResolver)
            }
        }
    }

    private fun editFilesystem(filesystem: Filesystem) {
        val editExisting = filesystem.name != ""
        val bundle = bundleOf("filesystem" to filesystem, "editExisting" to editExisting)
        this.findNavController().navigate(R.id.filesystem_edit_fragment, bundle)
    }

    private fun deleteFilesystem(filesystem: Filesystem) {
        val serviceIntent = Intent(activityContext, ServerService::class.java)
        serviceIntent.putExtra("type", "filesystemIsBeingDeleted")
        serviceIntent.putExtra("filesystemId", filesystem.id)
        activityContext.startService(serviceIntent)

        filesystemListViewModel.deleteFilesystemById(filesystem.id)
    }

    private fun exportFilesystem(filesystem: Filesystem) {
        val suggestedFilesystemBackupName = filesystemListViewModel.getFilesystemBackupName(filesystem)
        val intent = createExportExternalIntent(suggestedFilesystemBackupName)
        filesystemListViewModel.setFilesystemToBackup(filesystem)
        startActivityForResult(intent, FILESYSTEM_EXPORT_REQUEST_CODE)
    }

    private fun createExportExternalIntent(backupName: String): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/*"
            putExtra(Intent.EXTRA_TITLE, backupName)
        }
    }

    private fun handleExportStatus(viewState: FilesystemExportState) {
        return when (viewState) {
            is FilesystemExportState.Update -> {
                activityContext.updateFilesystemExportProgress(viewState.details)
            }
            is FilesystemExportState.Success -> {
                activityContext.stopProgressFromFilesystemList()
                Toast.makeText(activityContext, R.string.backup_export_success, Toast.LENGTH_LONG).show()
            }
            is FilesystemExportState.Failure -> {
                val dialogBuilder = AlertDialog.Builder(activityContext)
                val reason = if (viewState.reason == R.string.error_export_execution_failure) {
                    getString(viewState.reason, viewState.details)
                } else {
                    getString(viewState.reason)
                }
                dialogBuilder.setMessage(getString(R.string.export_failure) + "\n" + reason).create().show()
                activityContext.stopProgressFromFilesystemList()
            }
        }
    }

    private fun handleDeleteStatus(viewState: FilesystemDeleteState) {
        return when (viewState) {
            is FilesystemDeleteState.InProgress -> {
                activityContext.updateFilesystemDeleteProgress()
            }
            is FilesystemDeleteState.Success -> {
                activityContext.stopProgressFromFilesystemList()
            }
            is FilesystemDeleteState.Failure -> {
                activityContext.displayGenericErrorDialog(R.string.general_error_title, R.string.error_filesystem_delete)
                activityContext.stopProgressFromFilesystemList()
            }
        }
    }
}
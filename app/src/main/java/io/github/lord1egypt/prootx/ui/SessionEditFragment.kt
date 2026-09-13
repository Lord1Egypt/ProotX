package io.github.lord1egypt.prootx.ui

import android.app.Activity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.* // ktlint-disable no-wildcard-imports
import android.widget.* // ktlint-disable no-wildcard-imports
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.github.lord1egypt.prootx.databinding.FragSessionEditBinding
import io.github.lord1egypt.prootx.R
import io.github.lord1egypt.prootx.model.entities.Filesystem
import io.github.lord1egypt.prootx.model.entities.ServiceType
import io.github.lord1egypt.prootx.model.entities.Session
import io.github.lord1egypt.prootx.model.entities.toServiceType
import io.github.lord1egypt.prootx.model.repositories.ProotXDatabase
import io.github.lord1egypt.prootx.viewmodel.SessionEditViewModel
import io.github.lord1egypt.prootx.viewmodel.SessionEditViewmodelFactory

class SessionEditFragment : Fragment() {

    private var _binding: FragSessionEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var activityContext: Activity
    private val args: SessionEditFragmentArgs by navArgs()
    private val session: Session by lazy { args.session!! }
    private val editExisting by lazy { args.editExisting }

    private var filesystemList: List<Filesystem> = emptyList()

    private val sessionEditViewModel: SessionEditViewModel by lazy {
        val ulaDatabase = ProotXDatabase.getInstance(activityContext)
        ViewModelProvider(this, SessionEditViewmodelFactory(ulaDatabase)).get(SessionEditViewModel::class.java)
    }

    private val filesystemChangeObserver = Observer<List<Filesystem>> {
        it?.let { newList ->
            val spinnerList = augmentFilesystemList(newList)

            getListDifferenceAndSetNewFilesystem(filesystemList, it)

            val filesystemAdapter = ArrayAdapter(activityContext, android.R.layout.simple_spinner_dropdown_item, spinnerList)
            val filesystemNamePosition = it.indexOfFirst {
                filesystem -> filesystem.id == session.filesystemId
            }

            val usedPosition = if (filesystemNamePosition < 0) 0 else filesystemNamePosition

            binding.spinnerFilesystemList.adapter = filesystemAdapter
            binding.spinnerFilesystemList.setSelection(usedPosition)

            filesystemList = it
        }
    }

    internal sealed class FilesystemDropdownItem {
        data class NonFilesystemItem(val text: String) : FilesystemDropdownItem() {
            override fun toString(): String {
                return text
            }
        }
        data class FilesystemItem(val filesystem: Filesystem) : FilesystemDropdownItem() {
            override fun toString(): String {
                return "${filesystem.name}: ${filesystem.distributionType.capitalize()}"
            }
        }
    }

    private fun augmentFilesystemList(filesystems: List<Filesystem>): List<FilesystemDropdownItem> {
        val listBuilder = mutableListOf<FilesystemDropdownItem>()
        if (filesystems.isEmpty()) listBuilder.add(FilesystemDropdownItem.NonFilesystemItem(""))
        listBuilder.addAll(filesystems.map { FilesystemDropdownItem.FilesystemItem(it) })
        listBuilder.add(FilesystemDropdownItem.NonFilesystemItem("Create new"))
        return listBuilder.toList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragSessionEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.menu_item_add) {
            insertSession()
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activityContext = activity!!
        sessionEditViewModel.getAllFilesystems().observe(viewLifecycleOwner, filesystemChangeObserver)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textInputSessionName.setText(session.name)
        if (session.isAppsSession) {
            binding.textInputSessionName.isEnabled = false
        }
        binding.textInputSessionName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                session.name = p0.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        binding.spinnerFilesystemList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                parent?.let {
                    val item = parent.getItemAtPosition(position) as FilesystemDropdownItem
                    when (item) {
                        is FilesystemDropdownItem.NonFilesystemItem -> {
                            if (item.text == "Create new") {
                                val bundle = bundleOf("filesystem" to Filesystem(0), "editExisting" to false)
                                this@SessionEditFragment.findNavController()
                                        .navigate(R.id.filesystem_edit_fragment, bundle)
                            } else { }
                        }
                        is FilesystemDropdownItem.FilesystemItem -> {
                            val filesystem = item.filesystem
                            updateFilesystemDetailsForSession(filesystem)
                            binding.textInputUsername.setText(filesystem.defaultUsername)
                        }
                    }
                }
            }
        }

        binding.spinnerSessionServiceType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedServiceType = parent?.getItemAtPosition(position).toString().toServiceType()
                session.serviceType = selectedServiceType
            }
        }

        binding.textInputUsername.isEnabled = false
        binding.textInputUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                session.username = p0.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }

    private fun insertSession() {
        val navController = NavHostFragment.findNavController(this)

        if (session.name == "") {
            binding.textInputSessionName.error = getString(R.string.error_session_name)
        }
        if (session.filesystemName == "") {
            val errorText = binding.spinnerFilesystemList.selectedView as TextView
            errorText.error = ""
            errorText.setTextColor(Color.RED)
            errorText.text = getString(R.string.error_filesystem_name)
        }

        if (session.name == "" || session.username == "" || session.filesystemName == "") {
            Toast.makeText(activityContext, R.string.error_empty_field, Toast.LENGTH_LONG).show()
            return
        }

        if (editExisting) sessionEditViewModel.updateSession(session)
        else sessionEditViewModel.insertSession(session)
        navController.popBackStack()
    }

    private fun getDefaultServicePort(selectedServiceType: ServiceType): Long {
        return when (selectedServiceType) {
            ServiceType.Vnc -> 51
            else -> 2022
        }
    }

    private fun getListDifferenceAndSetNewFilesystem(prevFilesystems: List<Filesystem>, currentFilesystems: List<Filesystem>) {
        val uniqueFilesystems = currentFilesystems.subtract(prevFilesystems)
        if (prevFilesystems.isNotEmpty() && uniqueFilesystems.isNotEmpty()) {
            updateFilesystemDetailsForSession(uniqueFilesystems.first())
        }
    }

    private fun updateFilesystemDetailsForSession(filesystem: Filesystem) {
        session.filesystemName = filesystem.name
        session.username = filesystem.defaultUsername
        session.password = filesystem.defaultPassword
        session.vncPassword = filesystem.defaultVncPassword
        session.filesystemId = filesystem.id
    }
}
package io.github.lord1egypt.prootx.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.lord1egypt.prootx.databinding.FragHelpBinding

class HelpFragment : Fragment() {

    private var _binding: FragHelpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.githubLogo.setOnClickListener {
            val intent = Intent("android.intent.action.VIEW", Uri.parse("https://github.com/Lord1Egypt/ProotX/issues"))
            startActivity(intent)
        }

        binding.prootxLogo.setOnClickListener {
            val intent = Intent("android.intent.action.VIEW", Uri.parse("https://github.com/Lord1Egypt/ProotX"))
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
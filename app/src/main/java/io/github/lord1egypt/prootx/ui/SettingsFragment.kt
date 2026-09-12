package io.github.lord1egypt.prootx.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import io.github.lord1egypt.prootx.R
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.Preference
import io.github.lord1egypt.prootx.utils.ProotDebugLogger
import io.github.lord1egypt.prootx.utils.ProotXFiles
import io.github.lord1egypt.prootx.utils.defaultSharedPreferences

class SettingsFragment : PreferenceFragmentCompat() {

    private val prootDebugLogger by lazy {
        val prootxFiles = ProotXFiles(activity!!, activity!!.applicationInfo.nativeLibraryDir)
        ProotDebugLogger(activity!!.defaultSharedPreferences, prootxFiles)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)

        val deleteFilePreference: Preference = findPreference("pref_proot_delete_debug_file")!!
        deleteFilePreference.setOnPreferenceClickListener {
            prootDebugLogger.deleteLogs()
            true
        }

        val clearAutoStartPreference: Preference = findPreference("pref_clear_auto_start")!!
        clearAutoStartPreference.setOnPreferenceClickListener {
            val prefs = activity!!.getSharedPreferences("apps", Context.MODE_PRIVATE)
            with(prefs.edit()) {
                remove("AutoApp")
                apply()
                true
            }
        }
    }

    override fun setDivider(divider: Drawable?) {
        super.setDivider(ColorDrawable(Color.TRANSPARENT))
    }

    override fun setDividerHeight(height: Int) {
        super.setDividerHeight(0)
    }
}
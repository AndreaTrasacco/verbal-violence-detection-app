package it.unipi.masss.project.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.switchmaterial.SwitchMaterial
import it.unipi.masss.project.R
import it.unipi.masss.project.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val settingsPreferences = SettingsPreferences(requireContext())

        val auto_mon_sw = view.findViewById<SwitchMaterial>(R.id.auto_mon_sw)
        val close_contact_opt = view.findViewById<CheckBox>(R.id.close_contact_opt)
        val chosen_contact_opt = view.findViewById<CheckBox>(R.id.chosen_contact_opt)


        //load state of switch and chechboxes, set listener to trigger update of the state
        auto_mon_sw.isChecked = settingsPreferences.getAutoMonState()
        auto_mon_sw.setOnCheckedChangeListener { _, isChecked ->
            settingsPreferences.setAutoMonState(isChecked)
        }

        close_contact_opt.isChecked = settingsPreferences.getCloseContactOptionState()
        close_contact_opt.setOnCheckedChangeListener { _, isChecked ->
            settingsPreferences.setCloseContactOptionState(isChecked)
        }

        chosen_contact_opt.isChecked = settingsPreferences.getChosenContactOptionState()
        chosen_contact_opt.setOnCheckedChangeListener { _, isChecked ->
            settingsPreferences.setChosenContactOptionState(isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
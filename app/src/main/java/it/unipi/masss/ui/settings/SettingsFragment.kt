package it.unipi.masss.ui.settings

import SosContact
import SosContactAdapter
import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import it.unipi.masss.R
import it.unipi.masss.databinding.FragmentSettingsBinding
import it.unipi.masss.ui.recordings.AudioItem


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    val contactList = mutableListOf<SosContact>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val settingsPreferences = SettingsPreferences(requireContext())

        val autoMonSw = view.findViewById<SwitchMaterial>(R.id.auto_mon_sw)
        val closeContactOpt = view.findViewById<CheckBox>(R.id.close_contact_opt)
        val submitButton = view.findViewById<TextView>(R.id.submit_button)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        // Set the adapter for the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        val contactAdapter = SosContactAdapter(contactList)
        recyclerView.adapter = contactAdapter

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    Log.i(TAG, "Permission is granted")
                } else {
                    Log.i(TAG, "Permission not granted")
                }
            }

        val sharedPreferences = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val allEntries = sharedPreferences.all

        for ((key, value) in allEntries) {
            if (key.startsWith("contact_info_")) {
                val contactInfo = value as String
                val id = key.removePrefix("contact_info_").toLong()
                val (name, number) = contactInfo.split(",")
                val contact = SosContact(requireContext(), name, number, id)
                contactList.add(contact)
            }
        }

        val contactPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val id = ContentUris.parseId(uri)
                    val cursor = requireContext().contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id.toString()),
                        null
                    )

                    cursor?.use {
                        if (it.moveToFirst()) {
                            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            val name = it.getString(nameIndex)
                            val number = it.getString(numberIndex)

                            // Concatenate the name and number into a single string
                            val contactInfo = "$name,$number"

                            // Save the contact info to SharedPreferences
                            val existingContactInfo = sharedPreferences.getString("contact_info_$id", null)

                            if (existingContactInfo != contactInfo) {
                                val newContact = SosContact(requireContext(), name, number, id)
                                contactList.add(newContact)
                                contactAdapter.notifyItemInserted(contactList.size - 1)

                                val editor = sharedPreferences.edit()
                                editor.putString("contact_info_$id", contactInfo)
                                editor.apply()

                            } else {
                                Log.d(TAG, "Contact info is duplicate")
                            }
                        } else {
                            Log.d(TAG, "Cursor is empty")
                        }
                    } ?: Log.d(TAG, "Cursor is null")
                }
            }
        }

        // Set click listener for submit button
        submitButton.setOnClickListener {
            // Open the contact picker
            val hasPermission = ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_CONTACTS
            )== PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }

            val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            contactPickerLauncher.launch(intent)
        }

        // Load state of switch and checkboxes, set listeners to trigger updates
        autoMonSw.isChecked = settingsPreferences.getAutoMonState()
        autoMonSw.setOnCheckedChangeListener { _, isChecked ->
            settingsPreferences.setAutoMonState(isChecked)
        }

        closeContactOpt.isChecked = settingsPreferences.getCloseContactOptionState()
        closeContactOpt.setOnCheckedChangeListener { _, isChecked ->
            settingsPreferences.setCloseContactOptionState(isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "SettingsFragment"
    }
}

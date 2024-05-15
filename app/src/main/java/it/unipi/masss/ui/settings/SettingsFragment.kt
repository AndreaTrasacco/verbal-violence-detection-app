package it.unipi.masss.ui.settings

import SosContact
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
import com.google.android.material.switchmaterial.SwitchMaterial
import it.unipi.masss.R
import it.unipi.masss.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

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

        val auto_mon_sw = view.findViewById<SwitchMaterial>(R.id.auto_mon_sw)
        val close_contact_opt = view.findViewById<CheckBox>(R.id.close_contact_opt)
        val submitButton = view.findViewById<TextView>(R.id.submit_button)
        val contactListLayout = view.findViewById<LinearLayout>(R.id.contactList)
        val scrollViewLayout = view.findViewById<ScrollView>(R.id.scrollPage)

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    Log.i("Example", "Permission is granted")
                } else {
                    Log.i("Example", "Permission not granted")
                }
            }



        val sharedPreferences = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val allEntries = sharedPreferences.all

        for ((key, value) in allEntries) {
            if (key.startsWith("contact_info_")) {
                val contactInfo = value as String
                val id = key.removePrefix("contact_info_").toLong()
                val (name, number) = contactInfo.split(",")
                createContactTextView(id, name, number, requireContext(), contactListLayout, scrollViewLayout)
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
                                createContactTextView(id, name, number, requireContext(), contactListLayout, scrollViewLayout)
                                val editor = sharedPreferences.edit()
                                editor.putString("contact_info_$id", contactInfo)
                                editor.apply()
                            } else {
                                Log.d("ContactPicker", "Contact info is duplicate")
                            }
                        } else {
                            Log.d("ContactPicker", "Cursor is empty")
                        }
                    } ?: Log.d("ContactPicker", "Cursor is null")
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
        auto_mon_sw.isChecked = settingsPreferences.getAutoMonState()
        auto_mon_sw.setOnCheckedChangeListener { _, isChecked ->
            settingsPreferences.setAutoMonState(isChecked)
        }

        close_contact_opt.isChecked = settingsPreferences.getCloseContactOptionState()
        close_contact_opt.setOnCheckedChangeListener { _, isChecked ->
            settingsPreferences.setCloseContactOptionState(isChecked)
        }
    }


    private fun createContactTextView(id: Long, name: String, number: String, context: Context, contactListLayout: LinearLayout, scrollViewLayout: ScrollView) {
        val enteredText = "$name - $number"
        val sosContact = SosContact(context, enteredText, id)
        contactListLayout.addView(sosContact)
        contactListLayout.requestLayout()
        scrollViewLayout.requestLayout()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

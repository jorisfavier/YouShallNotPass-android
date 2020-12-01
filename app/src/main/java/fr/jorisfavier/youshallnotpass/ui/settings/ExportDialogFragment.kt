package fr.jorisfavier.youshallnotpass.ui.settings

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.ScrollView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import fr.jorisfavier.youshallnotpass.R

class ExportDialogFragment(val onExport: (String?) -> Unit) : DialogFragment() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var errorView: View
    private lateinit var passwordEditText: EditText
    private lateinit var passwordContainer: View
    private lateinit var scrollView: ScrollView


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return requireActivity().let {
            val customView = layoutInflater.inflate(R.layout.dialog_settings_export, null)
            radioGroup = customView.findViewById(R.id.settings_export_radioGrp)
            errorView = customView.findViewById(R.id.settings_export_dialogError)
            passwordEditText = customView.findViewById(R.id.settings_export_password)
            scrollView = customView.findViewById(R.id.exportDialogScrollView)
            passwordContainer = customView.findViewById(R.id.settings_export_password_container)

            customView.parent?.let {
                (it as? ViewGroup)?.removeView(customView)
            }
            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.export_password)
                .setView(customView)
                .setCancelable(false)
                .setPositiveButton(R.string.export, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    onExportClicked()
                }
            }
            radioGroup.setOnCheckedChangeListener { _, _ ->
                passwordContainer.isVisible = radioGroup.checkedRadioButtonId == R.id.settings_export_ysnp_button
            }
            dialog
        }
    }


    private fun onExportClicked() {
        val needPassword = radioGroup.checkedRadioButtonId == R.id.settings_export_ysnp_button
        val password = if (needPassword) passwordEditText.text?.toString() else null
        if (needPassword && password.isNullOrEmpty()) {
            errorView.isVisible = true
            scrollView.post {
                scrollView.fullScroll(View.FOCUS_DOWN)
            }
        } else {
            onExport(password)
            dismiss()
        }
    }
}
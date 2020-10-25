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

class ExportDialogFragment(val onExport: (Boolean, String) -> Unit) : DialogFragment() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var errorView: View
    private lateinit var passwordEditText: EditText
    private lateinit var passwordContainer: View
    private lateinit var scrollView: ScrollView


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return requireActivity().let {
            val customView = layoutInflater.inflate(R.layout.dialog_settings_export, null)
            radioGroup = customView.findViewById(R.id.settingsExportRadioGrp)
            errorView = customView.findViewById(R.id.exportDialogError)
            passwordEditText = customView.findViewById(R.id.exportPassword)
            scrollView = customView.findViewById(R.id.exportDialogScrollView)
            passwordContainer = customView.findViewById(R.id.exportPasswordContainer)

            customView.parent?.let {
                (it as? ViewGroup)?.removeView(customView)
            }
            val dialog = MaterialAlertDialogBuilder(context)
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
                passwordContainer.isVisible = radioGroup.checkedRadioButtonId == R.id.ysnpExportRadioButton
            }
            dialog
        }
    }


    private fun onExportClicked() {
        val password = passwordEditText.text?.toString()
        if (password.isNullOrEmpty()) {
            errorView.isVisible = true
            scrollView.post {
                scrollView.fullScroll(View.FOCUS_DOWN)
            }
        } else {
            val checkedId = radioGroup.checkedRadioButtonId
            onExport(checkedId == R.id.ysnpExportRadioButton, password)
            dismiss()
        }
    }
}
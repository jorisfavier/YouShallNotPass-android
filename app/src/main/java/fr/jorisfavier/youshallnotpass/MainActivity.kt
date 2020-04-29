package fr.jorisfavier.youshallnotpass

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import fr.jorisfavier.youshallnotpass.features.home.HomeViewModel
import fr.jorisfavier.youshallnotpass.features.search.SearchActivity

class MainActivity : AppCompatActivity() {

    private lateinit var viewmodel: HomeViewModel
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var biometricPromptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        YSNPApplication.currentInstance?.appComponent?.inject(this)
        viewmodel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        setContentView(R.layout.activity_main)
        initObserver()
        initAuthentication()
        displayAuthPrompt()
    }

    private fun initObserver() {
        viewmodel.authSuccess.observe(this, Observer<Boolean> { success ->
            if (success) {
                redirectToSearchPage()
            } else {
                displayErrorModal()
            }
        })
    }

    private fun displayAuthPrompt() {
        biometricPrompt.authenticate(biometricPromptInfo)
    }

    private fun redirectToSearchPage() {
        val searchPahgeIntent = Intent(this, SearchActivity::class.java)
        startActivity(searchPahgeIntent)
        finish()
    }

    private fun displayErrorModal() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("Authentication failed")
        builder.setMessage("Authentication failed please try again.")
        builder.setPositiveButton(android.R.string.yes) { _, _ ->
            displayAuthPrompt()
        }
        builder.show()
    }

    private fun initAuthentication() {
        biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.authentication_required))
                .setNegativeButtonText(getString(android.R.string.cancel))
                .build()
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, viewmodel.authCallback)
    }

}

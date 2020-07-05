package fr.jorisfavier.youshallnotpass.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.AndroidInjection
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.ui.home.HomeActivity
import javax.inject.Inject

class AuthActivity : AppCompatActivity() {

    companion object {
        const val redirectToHomeExtraKey = "redirectToHome"
    }

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var biometricPromptInfo: BiometricPrompt.PromptInfo
    private var redirectToHome = true

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: AuthViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        AndroidInjection.inject(this)
        initObserver()
        initAuthentication()
        displayAuthPrompt()
        redirectToHome = intent.getBooleanExtra(redirectToHomeExtraKey, true)
    }

    private fun initObserver() {
        viewModel.authSuccess.observe(this, Observer { success ->
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
        if (redirectToHome) {
            val searchPageIntent = Intent(this, HomeActivity::class.java)
            startActivity(searchPageIntent)
        }
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
        biometricPrompt = BiometricPrompt(this, executor, viewModel.authCallback)
    }

}

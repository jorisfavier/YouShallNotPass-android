package fr.jorisfavier.youshallnotpass.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.AndroidInjection
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.ui.home.HomeActivity
import javax.inject.Inject

class AuthActivity : AppCompatActivity(R.layout.activity_auth) {

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var biometricPromptInfo: BiometricPrompt.PromptInfo
    private var redirectToHome = true

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: AuthViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        initObserver()
        initAuthentication()
        displayAuthPrompt()
        redirectToHome = intent.getBooleanExtra(REDIRECT_TO_HOME_EXTRA, true)
        supportActionBar?.hide()
    }

    private fun initObserver() {
        viewModel.authSuccess.observe(this) { success ->
            if (success) {
                redirectToSearchPage()
            } else {
                displayErrorModal()
            }
        }
    }

    private fun displayAuthPrompt() {
        if (viewModel.isDeviceSecure()) {
            biometricPrompt.authenticate(biometricPromptInfo)
        } else {
            displayErrorModal(titleResId = R.string.device_not_secure, messageResId = R.string.device_not_secure_message)
        }
    }

    private fun redirectToSearchPage() {
        if (redirectToHome) {
            val searchPageIntent = Intent(this, HomeActivity::class.java)
            startActivity(searchPageIntent)
        }
        ActivityCompat.finishAffinity(this)
    }

    private fun displayErrorModal(
        @StringRes titleResId: Int = R.string.auth_fail,
        @StringRes messageResId: Int = R.string.auth_fail_try_again
    ) {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(getString(titleResId))
        builder.setMessage(getString(messageResId))
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            displayAuthPrompt()
        }
        builder.show()
    }

    private fun initAuthentication() {
        biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.authentication_required))
            .setDeviceCredentialAllowed(true)
            .build()
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, viewModel.authCallback)
    }

    companion object {
        const val REDIRECT_TO_HOME_EXTRA = "redirectToHome"
    }
}

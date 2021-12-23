package fr.jorisfavier.youshallnotpass.ui.auth

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.ui.home.HomeActivity
import fr.jorisfavier.youshallnotpass.utils.observeEvent

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var biometricPromptInfo: BiometricPrompt.PromptInfo
    private var redirectToHome = true

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_auth)
        initObserver()
        initAuthentication()
        redirectToHome = intent.getBooleanExtra(REDIRECT_TO_HOME_EXTRA, true)
        supportActionBar?.hide()
    }

    override fun onResume() {
        super.onResume()
        viewModel.requestAuthentication()
    }

    override fun onBackPressed() {
        ActivityCompat.finishAffinity(this)
    }

    private fun initObserver() {
        viewModel.authStatus.observeEvent(this) { authResult ->
            when (authResult) {
                is AuthViewModel.AuthStatus.Ready -> displayAuthPrompt()
                is AuthViewModel.AuthStatus.Failure -> {
                    displayErrorModal(messageResId = authResult.errorMessage)
                }
                is AuthViewModel.AuthStatus.Success -> {
                    redirectToSearchPage()
                }
                is AuthViewModel.AuthStatus.SetupBiometric -> {
                    // Prompts the user to create credentials
                    val enrollIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                    displayErrorModal(
                        titleResId = R.string.setup_biometric,
                        messageResId = R.string.please_setup_biometric,
                        actionButtonResId = R.string.register_credentials,
                        actionButtonCallback = { startActivity(enrollIntent) }
                    )
                }
                is AuthViewModel.AuthStatus.NonSecure -> {
                    val enrollIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                    displayErrorModal(
                        titleResId = R.string.device_not_secure,
                        messageResId = R.string.device_not_secure_message,
                        actionButtonResId = R.string.setup_security,
                        actionButtonCallback = { startActivity(enrollIntent) }
                    )
                }
                is AuthViewModel.AuthStatus.NoBiometric -> {
                    displayErrorModal(
                        titleResId = R.string.device_not_compatible,
                        messageResId = R.string.unfortunately_you_cant_use_the_app,
                        actionButtonCallback = { finishAffinity() }
                    )
                }
            }
        }
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

    private fun displayErrorModal(
        @StringRes titleResId: Int = R.string.auth_fail,
        @StringRes messageResId: Int = R.string.auth_fail_try_again,
        @StringRes actionButtonResId: Int = android.R.string.ok,
        actionButtonCallback: () -> Unit = ::displayAuthPrompt,
    ) {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(getString(titleResId))
        builder.setMessage(getString(messageResId))
        builder.setPositiveButton(actionButtonResId) { _, _ ->
            actionButtonCallback()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun initAuthentication() {
        biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.authentication_required))
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
            .setNegativeButtonText(getString(android.R.string.cancel))
            .build()
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, viewModel.authCallback)
    }

    companion object {
        const val REDIRECT_TO_HOME_EXTRA = "redirectToHome"
    }
}

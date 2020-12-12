package fr.jorisfavier.youshallnotpass

import android.app.KeyguardManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.biometric.BiometricPrompt
import fr.jorisfavier.youshallnotpass.manager.IAuthManager
import fr.jorisfavier.youshallnotpass.ui.auth.AuthViewModel
import fr.jorisfavier.youshallnotpass.utils.getOrAwaitValue
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `authCallback should emit Success onAuthenticationSucceeded`() {
        //given
        val authManager: IAuthManager = mockk()
        val keyguardManager: KeyguardManager = mockk()
        every { authManager setProperty "isUserAuthenticated" value true } just runs

        val viewModel = AuthViewModel(authManager, keyguardManager)

        //when
        viewModel.authCallback.onAuthenticationSucceeded(mockk())

        //then
        assertEquals(viewModel.authSuccess.getOrAwaitValue().peekContent(), AuthViewModel.AuthResult.Success)
        verify { authManager setProperty "isUserAuthenticated" value true }
    }

    @Test
    fun `authCallback should emit Failure onAuthenticationError`() {
        //given
        val authManager: IAuthManager = mockk()
        val keyguardManager: KeyguardManager = mockk()

        val viewModel = AuthViewModel(authManager, keyguardManager)

        //when
        viewModel.authCallback.onAuthenticationError(BiometricPrompt.ERROR_UNABLE_TO_PROCESS, "")

        //then
        val authResult = viewModel.authSuccess.getOrAwaitValue().peekContent() as? AuthViewModel.AuthResult.Failure
        assertTrue(authResult is AuthViewModel.AuthResult.Failure)
        assertEquals(authResult?.errorMessage, R.string.auth_fail_try_again)
    }

    @Test
    fun `authCallback should emit Failure with no biometric message onAuthenticationError with ERROR_NO_BIOMETRICS code`() {
        //given
        val authManager: IAuthManager = mockk()
        val keyguardManager: KeyguardManager = mockk()

        val viewModel = AuthViewModel(authManager, keyguardManager)

        //when
        viewModel.authCallback.onAuthenticationError(BiometricPrompt.ERROR_NO_BIOMETRICS, "")

        //then
        val authResult = viewModel.authSuccess.getOrAwaitValue().peekContent() as? AuthViewModel.AuthResult.Failure
        assertTrue(authResult is AuthViewModel.AuthResult.Failure)
        assertEquals(authResult?.errorMessage, R.string.auth_fail_no_biometrics)
    }

    @Test
    fun `authCallback should emit Failure onAuthenticationFailed`() {
        //given
        val authManager: IAuthManager = mockk()
        val keyguardManager: KeyguardManager = mockk()

        val viewModel = AuthViewModel(authManager, keyguardManager)

        //when
        viewModel.authCallback.onAuthenticationFailed()

        //then
        assertTrue(viewModel.authSuccess.getOrAwaitValue().peekContent() is AuthViewModel.AuthResult.Failure)
    }
}
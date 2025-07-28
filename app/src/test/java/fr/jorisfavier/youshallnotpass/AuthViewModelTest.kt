package fr.jorisfavier.youshallnotpass

import android.app.KeyguardManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import fr.jorisfavier.youshallnotpass.manager.AuthManager
import fr.jorisfavier.youshallnotpass.ui.auth.AuthStatus
import fr.jorisfavier.youshallnotpass.ui.auth.AuthViewModel
import fr.jorisfavier.youshallnotpass.utils.MainDispatcherRule
import fr.jorisfavier.youshallnotpass.utils.getOrAwaitValue
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainDispatcherRule()

    @Test
    fun `authCallback should emit Success onAuthenticationSucceeded`() = runTest {
        //given
        val authManager: AuthManager = mockk()
        val keyguardManager: KeyguardManager = mockk()
        val biometricManager: BiometricManager = mockk()
        every { authManager setProperty "isUserAuthenticated" value true } just runs

        val viewModel = AuthViewModel(authManager, keyguardManager, biometricManager)

        //when
        viewModel.authCallback.onAuthenticationSucceeded(mockk())

        //then
        assertEquals(
            viewModel.authStatus.getOrAwaitValue().peekContent(),
            AuthStatus.Success
        )
        verify { authManager setProperty "isUserAuthenticated" value true }
    }

    @Test
    fun `authCallback should emit Failure onAuthenticationError`() = runTest {
        //given
        val authManager: AuthManager = mockk()
        val keyguardManager: KeyguardManager = mockk()
        val biometricManager: BiometricManager = mockk()

        val viewModel = AuthViewModel(authManager, keyguardManager, biometricManager)

        //when
        viewModel.authCallback.onAuthenticationError(BiometricPrompt.ERROR_UNABLE_TO_PROCESS, "")

        //then
        val authResult = viewModel.authStatus.getOrAwaitValue()
            .peekContent() as? AuthStatus.Failure
        assertTrue(authResult is AuthStatus.Failure)
        assertEquals(authResult?.errorMessage, R.string.auth_fail_try_again)
    }

    @Test
    fun `authCallback should emit Failure with no biometric message onAuthenticationError with ERROR_NO_BIOMETRICS code`() =
        runTest {
            //given
            val authManager: AuthManager = mockk()
            val keyguardManager: KeyguardManager = mockk()
            val biometricManager: BiometricManager = mockk()

            val viewModel = AuthViewModel(authManager, keyguardManager, biometricManager)

            //when
            viewModel.authCallback.onAuthenticationError(BiometricPrompt.ERROR_NO_BIOMETRICS, "")

            //then
            val authResult = viewModel.authStatus.getOrAwaitValue()
                .peekContent() as? AuthStatus.Failure
            assertTrue(authResult is AuthStatus.Failure)
            assertEquals(authResult?.errorMessage, R.string.auth_fail_no_biometrics)
        }

    @Test
    fun `authCallback should emit Failure onAuthenticationFailed`() = runTest {
        //given
        val authManager: AuthManager = mockk()
        val keyguardManager: KeyguardManager = mockk()
        val biometricManager: BiometricManager = mockk()

        val viewModel = AuthViewModel(authManager, keyguardManager, biometricManager)

        //when
        viewModel.authCallback.onAuthenticationFailed()

        //then
        assertTrue(
            viewModel.authStatus.getOrAwaitValue().peekContent() is AuthStatus.Failure
        )
    }
}

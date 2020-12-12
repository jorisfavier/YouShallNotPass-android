package fr.jorisfavier.youshallnotpass

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.jorisfavier.youshallnotpass.manager.IAuthManager
import fr.jorisfavier.youshallnotpass.manager.impl.AuthManager
import fr.jorisfavier.youshallnotpass.ui.home.HomeViewModel
import fr.jorisfavier.youshallnotpass.utils.getOrAwaitValue
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeoutException

class HomeViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `user should not be authenticated when onAppPaused`() {
        //given
        val authManager: IAuthManager = mockk()
        every { authManager setProperty "isUserAuthenticated" value false } just runs
        val viewModel = HomeViewModel(authManager)

        //when
        viewModel.onAppPaused()

        //then
        verify { authManager setProperty "isUserAuthenticated" value false }
    }

    @Test
    fun `requireAuthentication should emit a value onAppResumed if the user is not authenticated`() {
        //given
        val authManager: IAuthManager = mockk()
        every { authManager getProperty "isUserAuthenticated" } returns false
        val viewModel = HomeViewModel(authManager)

        //when
        viewModel.onAppResumed()

        //then
        assertEquals(Unit, viewModel.requireAuthentication.getOrAwaitValue())
        verify { authManager getProperty "isUserAuthenticated" }
    }

    @Test(expected = TimeoutException::class)
    fun `requireAuthentication should not emit a value on configuration change`() {
        //given
        val authManager: IAuthManager = AuthManager().apply { isUserAuthenticated = true }
        val viewModel = HomeViewModel(authManager)

        //when
        viewModel.onAppPaused()
        viewModel.onConfigurationChanged()
        viewModel.onAppResumed()

        //then
        assertEquals(true, authManager.isUserAuthenticated)
        viewModel.requireAuthentication.getOrAwaitValue()
    }

    @Test(expected = TimeoutException::class)
    fun `requireAuthentication should not emit a value when ignoreNextPause`() {
        //given
        val authManager: IAuthManager = AuthManager().apply { isUserAuthenticated = true }
        val viewModel = HomeViewModel(authManager)

        //when
        viewModel.ignoreNextPause()
        viewModel.onAppPaused()
        viewModel.onAppResumed()

        //then
        assertEquals(true, authManager.isUserAuthenticated)
        viewModel.requireAuthentication.getOrAwaitValue()
    }
}
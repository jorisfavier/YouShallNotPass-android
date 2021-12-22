package fr.jorisfavier.youshallnotpass

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.jorisfavier.youshallnotpass.analytics.ScreenName
import fr.jorisfavier.youshallnotpass.analytics.YSNPAnalytics
import fr.jorisfavier.youshallnotpass.analytics.impl.YSNPAnalyticsImpl
import fr.jorisfavier.youshallnotpass.api.AnalyticsApi
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource
import fr.jorisfavier.youshallnotpass.manager.AuthManager
import fr.jorisfavier.youshallnotpass.manager.impl.AuthManagerImpl
import fr.jorisfavier.youshallnotpass.ui.home.HomeViewModel
import fr.jorisfavier.youshallnotpass.utils.getOrAwaitValue
import io.mockk.*
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import java.util.concurrent.TimeoutException

class HomeViewModelTest {

    private val authManager: AuthManager = mockk()
    val analyticsApi: AnalyticsApi = mockk()
    val appPreference: AppPreferenceDataSource = mockk()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `user should not be authenticated when onAppPaused`() {
        //given
        every { authManager setProperty "isUserAuthenticated" value false } just runs
        val viewModel = HomeViewModel(authManager, analytics = mockk())

        //when
        viewModel.onAppPaused()

        //then
        verify { authManager setProperty "isUserAuthenticated" value false }
    }

    @Test
    fun `requireAuthentication should emit a value onAppResumed if the user is not authenticated`() {
        //given
        every { authManager getProperty "isUserAuthenticated" } returns false
        val viewModel = HomeViewModel(authManager, analytics = mockk())

        //when
        viewModel.onAppResumed()

        //then
        assertEquals(Unit, viewModel.requireAuthentication.getOrAwaitValue())
        verify { authManager getProperty "isUserAuthenticated" }
    }

    @Test(expected = TimeoutException::class)
    fun `requireAuthentication should not emit a value on configuration change`() {
        //given
        val authManager: AuthManager = AuthManagerImpl().apply { isUserAuthenticated = true }
        authManager.isUserAuthenticated = true
        val viewModel = HomeViewModel(authManager, analytics = mockk())

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
        val authManager: AuthManager = AuthManagerImpl().apply { isUserAuthenticated = true }
        val viewModel = HomeViewModel(authManager, analytics = mockk())

        //when
        viewModel.ignoreNextPause()
        viewModel.onAppPaused()
        viewModel.onAppResumed()

        //then
        assertEquals(true, authManager.isUserAuthenticated)
        viewModel.requireAuthentication.getOrAwaitValue()
    }

    @Test
    fun `tracking the home screen for the first time is working`() {
        //given
        coEvery { analyticsApi.sendEvent(any()) } just runs
        coEvery { appPreference.getAnalyticEventDate(any()) } returns null
        coEvery { appPreference.setAnalyticEventDate(any(), any()) } just runs
        val analytics = YSNPAnalyticsImpl(analyticsApi, appPreference)
        val viewModel = HomeViewModel(authManager, analytics = analytics)

        //when
        viewModel.trackScreenView()

        //then
        coVerify { analyticsApi.sendEvent(ScreenName.Home.event) }
        coVerify { appPreference.setAnalyticEventDate(ScreenName.Home, any()) }
    }

    @Test
    fun `tracking the home screen should only happen once per day`() {
        //given
        coEvery { analyticsApi.sendEvent(any()) } just runs
        coEvery { appPreference.getAnalyticEventDate(any()) } returns LocalDateTime.now()
        coEvery { appPreference.setAnalyticEventDate(any(), any()) } just runs
        val analytics = YSNPAnalyticsImpl(analyticsApi, appPreference)
        val viewModel = HomeViewModel(authManager, analytics = analytics)

        //when
        viewModel.trackScreenView()

        //then
        coVerify(inverse = true) { analyticsApi.sendEvent(ScreenName.Home.event) }
        coVerify(inverse = true) { appPreference.setAnalyticEventDate(ScreenName.Home, any()) }
    }
}
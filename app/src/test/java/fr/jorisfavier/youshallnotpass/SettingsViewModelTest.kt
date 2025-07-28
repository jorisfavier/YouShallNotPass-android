package fr.jorisfavier.youshallnotpass

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.model.ExternalItem
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.repository.AppPreferenceRepository
import fr.jorisfavier.youshallnotpass.repository.ExternalItemRepository
import fr.jorisfavier.youshallnotpass.repository.ItemRepository
import fr.jorisfavier.youshallnotpass.ui.settings.SettingsViewModel
import fr.jorisfavier.youshallnotpass.utils.CoroutineDispatchers
import fr.jorisfavier.youshallnotpass.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainDispatcherRule()

    private val itemRepository: ItemRepository = mockk()
    private val cryptoManager: CryptoManager = mockk()
    private val appPreferences: AppPreferenceRepository = mockk<AppPreferenceRepository>().apply {
        every { shouldHideItems } returns flowOf(false)
        every { theme } returns flowOf(null)
    }
    private val externalItemRepository: ExternalItemRepository = mockk()
    private val coroutineDispatchers = CoroutineDispatchers(
        default = Dispatchers.Main,
        io = Dispatchers.Main,
        unconfined = Dispatchers.Main,
    )
    private val viewModel = SettingsViewModel(
        appPreferenceRepository = appPreferences,
        itemRepository = itemRepository,
        externalItemRepository = externalItemRepository,
        cryptoManager = cryptoManager,
        dispatchers = coroutineDispatchers,
    )

    private val fakeItem = Item(
        id = 1,
        title = "Test title",
        login = "test login",
        password = ByteArray(0),
        initializationVector = ByteArray(0)
    )
    private val fakePassword = "fake password"

    @Test
    fun `exportPassword without password should emit an Intent containing a file Uri`() =
        runTest {
            //given
            val externalItemSlot = slot<List<ExternalItem>>()
            coEvery { cryptoManager.decryptData(any(), any()) } returns Result.success(fakePassword)
            coEvery {
                externalItemRepository.saveExternalItems(
                    capture(externalItemSlot),
                    null
                )
            } returns mockk()
            coEvery { itemRepository.getAllItems() } returns flow { emit(listOf(fakeItem)) }

            //when
            viewModel.exportPasswords(null).first()
            advanceUntilIdle()

            //then
            TestCase.assertEquals(fakeItem.title, externalItemSlot.captured.first().title)
            TestCase.assertEquals(fakeItem.login, externalItemSlot.captured.first().login)
            TestCase.assertEquals(fakePassword, externalItemSlot.captured.first().password)
        }

    @Test
    fun `exportPassword with password should emit an Intent containing a file Uri`() =
        runTest {
            //given
            val externalItemSlot = slot<List<ExternalItem>>()
            val passwordSlot = slot<String>()
            val filePassword = "test"
            coEvery { cryptoManager.decryptData(any(), any()) } returns Result.success(fakePassword)
            coEvery {
                externalItemRepository.saveExternalItems(
                    capture(externalItemSlot),
                    capture(passwordSlot),
                )
            } returns mockk()
            coEvery { itemRepository.getAllItems() } returns flow { emit(listOf(fakeItem)) }
            every { appPreferences.shouldHideItems } returns flow { emit(false) }

            //when
            viewModel.exportPasswords(filePassword).first()

            //then
            TestCase.assertEquals(filePassword, passwordSlot.captured)
            TestCase.assertEquals(fakeItem.title, externalItemSlot.captured.first().title)
            TestCase.assertEquals(fakeItem.login, externalItemSlot.captured.first().login)
            TestCase.assertEquals(fakePassword, externalItemSlot.captured.first().password)
        }

    @Test
    fun `deleteAllItems should delete all items`() = runTest {
        //given
        coEvery { itemRepository.deleteAllItems() } returns Result.success(Unit)

        //when
        val result = viewModel.deleteAllItems().first()

        //then
        TestCase.assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteAllItems with an exception thrown from the repository should emit a failure`() =
        runTest {
            //given
            coEvery { itemRepository.deleteAllItems() } throws Exception()

            //when
            val result = viewModel.deleteAllItems().first()

            //then
            TestCase.assertTrue(result.isFailure)
        }
}

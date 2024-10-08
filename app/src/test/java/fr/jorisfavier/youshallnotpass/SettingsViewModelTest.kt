package fr.jorisfavier.youshallnotpass

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.model.ExternalItem
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.repository.ExternalItemRepository
import fr.jorisfavier.youshallnotpass.repository.ItemRepository
import fr.jorisfavier.youshallnotpass.ui.settings.SettingsViewModel
import fr.jorisfavier.youshallnotpass.utils.CoroutineDispatchers
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val itemRepository: ItemRepository = mockk()
    private val cryptoManager: CryptoManager = mockk()
    private val appPreferences: AppPreferenceDataSource = mockk()
    private val externalItemRepository: ExternalItemRepository = mockk()
    private val coroutineDispatchers = CoroutineDispatchers(
        default = Dispatchers.Main,
        io = Dispatchers.Main,
        unconfined = Dispatchers.Main,
    )
    private val viewModel = SettingsViewModel(
        appPreferences,
        itemRepository,
        externalItemRepository,
        cryptoManager,
        coroutineDispatchers,
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
        runBlocking {
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

            //then
            TestCase.assertEquals(fakeItem.title, externalItemSlot.captured.first().title)
            TestCase.assertEquals(fakeItem.login, externalItemSlot.captured.first().login)
            TestCase.assertEquals(fakePassword, externalItemSlot.captured.first().password)
        }

    @Test
    fun `exportPassword with password should emit an Intent containing a file Uri`() = runBlocking {
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

        //when
        viewModel.exportPasswords(filePassword).first()

        //then
        TestCase.assertEquals(filePassword, passwordSlot.captured)
        TestCase.assertEquals(fakeItem.title, externalItemSlot.captured.first().title)
        TestCase.assertEquals(fakeItem.login, externalItemSlot.captured.first().login)
        TestCase.assertEquals(fakePassword, externalItemSlot.captured.first().password)
    }

    @Test
    fun `deleteAllItems should delete all items`() = runBlocking {
        //given
        coEvery { itemRepository.deleteAllItems() } returns Result.success(Unit)

        //when
        val result = viewModel.deleteAllItems().first()

        //then
        TestCase.assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteAllItems with an exception thrown from the repository should emit a failure`() =
        runBlocking {
            //given
            coEvery { itemRepository.deleteAllItems() } throws Exception()

            //when
            val result = viewModel.deleteAllItems().first()

            //then
            TestCase.assertTrue(result.isFailure)
        }
}

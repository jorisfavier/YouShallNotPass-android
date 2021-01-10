package fr.jorisfavier.youshallnotpass

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.manager.model.EncryptedData
import fr.jorisfavier.youshallnotpass.model.ExternalItem
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.repository.IExternalItemRepository
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.ImportItemViewModel
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.ImportItemViewModel.Companion.PASSWORD_NEEDED_SLIDE
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.ImportItemViewModel.Companion.REVIEW_ITEM_SLIDE
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.ImportItemViewModel.Companion.SUCCESS_FAIL_SLIDE
import fr.jorisfavier.youshallnotpass.utils.State
import fr.jorisfavier.youshallnotpass.utils.getOrAwaitValue
import io.mockk.*
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class ImportItemViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val itemRepository: IItemRepository = mockk()
    private val cryptoManager: ICryptoManager = mockk()
    private val externalItemRepository: IExternalItemRepository = mockk()
    private val viewModel = ImportItemViewModel(externalItemRepository, cryptoManager, itemRepository)

    private val fakeItem = ExternalItem(
        title = "Test title",
        login = "test login",
        password = "fakePassword"
    )
    private val fakeEncryptedData = EncryptedData(ByteArray(1), ByteArray(1))
    private val fakePassword = "fake password"

    @Test
    fun `setUri with a ysnp file uri should emit a navigate event`() = runBlocking {
        //given
        val uri: Uri = mockk()
        coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns true

        //when
        viewModel.setUri(uri)

        //then
        TestCase.assertTrue(viewModel.isSecureFile.value ?: false)
        TestCase.assertTrue(viewModel.isFileSelected)
        TestCase.assertEquals(Unit, viewModel.navigate.getOrAwaitValue().peekContent())
    }

    @Test
    fun `setUri with a csv file uri should emit a navigate event`() = runBlocking {
        //given
        val uri: Uri = mockk()
        coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns false

        //when
        viewModel.setUri(uri)

        //then
        TestCase.assertFalse(viewModel.isSecureFile.value ?: true)
        TestCase.assertTrue(viewModel.isFileSelected)
        TestCase.assertEquals(Unit, viewModel.navigate.getOrAwaitValue().peekContent())
    }

    @Test
    fun `onSlideChanged with PASSWORD_NEEDED_SLIDE position should emit a navigate event if isSecureFile is false`() = runBlocking {
        //given
        val uri: Uri = mockk()
        coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns false
        var count = 0

        //when
        viewModel.navigate.observeForever {
            it.getContentIfNotHandled()?.let {
                count++
            }
        }
        viewModel.setUri(uri)
        viewModel.onSlideChanged(PASSWORD_NEEDED_SLIDE)

        //then
        TestCase.assertFalse(viewModel.isSecureFile.value ?: true)
        TestCase.assertEquals(2, count)
    }

    @Test
    fun `onSlideChanged with PASSWORD_NEEDED_SLIDE position should not emit a navigate event if isSecureFile is true`() = runBlocking {
        //given
        val uri: Uri = mockk()
        coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns true
        var count = 0

        //when
        viewModel.navigate.observeForever {
            it.getContentIfNotHandled()?.let {
                count++
            }
        }
        viewModel.setUri(uri)
        viewModel.onSlideChanged(PASSWORD_NEEDED_SLIDE)

        //then
        TestCase.assertTrue(viewModel.isSecureFile.value ?: false)
        TestCase.assertEquals(1, count)
    }

    @Test
    fun `onSlideChanged with REVIEW_ITEM_SLIDE position should load external items`() = runBlocking {
        //given
        val uri: Uri = mockk()
        val filePassword = slot<String>()
        coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns true
        coEvery { externalItemRepository.getExternalItemsFromUri(any(), capture(filePassword)) } returns listOf(fakeItem)
        viewModel.password.value = fakePassword
        val states = mutableListOf<State>()

        //when
        viewModel.loadFromUriState.observeForever { event ->
            event.getContentIfNotHandled()?.let { states.add(it) }
        }
        viewModel.setUri(uri)
        viewModel.onSlideChanged(REVIEW_ITEM_SLIDE)

        //then
        TestCase.assertTrue(viewModel.isSecureFile.value ?: false)
        TestCase.assertEquals(fakePassword, filePassword.captured)
        TestCase.assertEquals(2, states.size)
        TestCase.assertTrue(states[0] is State.Loading)
        TestCase.assertTrue(states[1] is State.Success)
        TestCase.assertEquals(fakeItem, viewModel.importedItems.value?.firstOrNull()?.externalItem)
    }

    @Test
    fun `onSlideChanged with REVIEW_ITEM_SLIDE position and empty item list should emit error`() = runBlocking {
        //given
        val uri: Uri = mockk()
        val filePassword = slot<String>()
        coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns true
        coEvery { externalItemRepository.getExternalItemsFromUri(any(), capture(filePassword)) } returns listOf()
        viewModel.password.value = fakePassword
        val states = mutableListOf<State>()

        //when
        viewModel.loadFromUriState.observeForever { event ->
            event.getContentIfNotHandled()?.let { states.add(it) }
        }
        viewModel.setUri(uri)
        viewModel.onSlideChanged(REVIEW_ITEM_SLIDE)

        //then
        TestCase.assertTrue(viewModel.isSecureFile.value ?: false)
        TestCase.assertEquals(fakePassword, filePassword.captured)
        TestCase.assertEquals(2, states.size)
        TestCase.assertTrue(states[0] is State.Loading)
        TestCase.assertTrue(states[1] is State.Error)
        TestCase.assertEquals(0, viewModel.importedItems.value?.size)
    }

    @Test
    fun `onSlideChanged with REVIEW_ITEM_SLIDE position and exception from the repository should emit an error`() = runBlocking {
        //given
        val uri: Uri = mockk()
        val filePassword = slot<String>()
        coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns true
        coEvery { externalItemRepository.getExternalItemsFromUri(any(), capture(filePassword)) } throws Exception()
        viewModel.password.value = fakePassword
        val states = mutableListOf<State>()
        var count = 0

        //when
        viewModel.navigate.observeForever { event ->
            event.getContentIfNotHandled()?.let { count++ }
        }
        viewModel.loadFromUriState.observeForever { event ->
            event.getContentIfNotHandled()?.let { states.add(it) }
        }
        viewModel.setUri(uri)
        viewModel.onSlideChanged(REVIEW_ITEM_SLIDE)

        //then
        TestCase.assertTrue(viewModel.isSecureFile.value ?: false)
        TestCase.assertEquals(fakePassword, filePassword.captured)
        TestCase.assertEquals(2, states.size)
        TestCase.assertTrue(states[0] is State.Loading)
        TestCase.assertTrue(states[1] is State.Error)
        TestCase.assertEquals(0, viewModel.importedItems.value?.size)
        TestCase.assertEquals(2, count)
    }

    @Test
    fun `onSlideChanged with SUCCESS_FAIL_SLIDE position and selected items should import items`() = runBlocking {
        //given
        val uri: Uri = mockk()
        val items = slot<List<Item>>()
        coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns false
        coEvery { externalItemRepository.getExternalItemsFromUri(any(), any()) } returns listOf(fakeItem)
        coEvery { cryptoManager.encryptData(any()) } returns fakeEncryptedData
        coEvery { itemRepository.insertItems(capture(items)) } just runs
        val states = mutableListOf<State>()

        //when
        viewModel.importItemsState.observeForever { event ->
            event.getContentIfNotHandled()?.let { states.add(it) }
        }
        viewModel.setUri(uri)
        viewModel.onSlideChanged(REVIEW_ITEM_SLIDE)
        viewModel.importedItems.value?.forEach { it.selected = true }
        viewModel.onSlideChanged(SUCCESS_FAIL_SLIDE)

        //then
        TestCase.assertEquals(2, states.size)
        TestCase.assertTrue(states[0] is State.Loading)
        TestCase.assertTrue(states[1] is State.Success)
        TestCase.assertEquals(fakeItem.title, items.captured.first().title)
        TestCase.assertEquals(fakeItem.login, items.captured.first().login)
        TestCase.assertEquals(fakeEncryptedData.initializationVector, items.captured.first().initializationVector)
        TestCase.assertEquals(fakeEncryptedData.ciphertext, items.captured.first().password)
        TestCase.assertEquals(0, items.captured.first().id)
    }

    @Test
    fun `onSlideChanged with SUCCESS_FAIL_SLIDE position and no item selected should emit an error`() = runBlocking {
        //given
        val uri: Uri = mockk()
        coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns false
        coEvery { externalItemRepository.getExternalItemsFromUri(any(), any()) } returns listOf(fakeItem)
        val states = mutableListOf<State>()

        //when
        viewModel.importItemsState.observeForever { event ->
            event.getContentIfNotHandled()?.let { states.add(it) }
        }
        viewModel.setUri(uri)
        viewModel.onSlideChanged(REVIEW_ITEM_SLIDE)
        viewModel.importedItems.value?.forEach { it.selected = false }
        viewModel.onSlideChanged(SUCCESS_FAIL_SLIDE)

        //then
        TestCase.assertEquals(2, states.size)
        TestCase.assertTrue(states[0] is State.Loading)
        TestCase.assertTrue(states[1] is State.Error)
    }

    @Test
    fun `onSlideChanged with SUCCESS_FAIL_SLIDE position and an exception from the repository should emit an error`() = runBlocking {
        //given
        val uri: Uri = mockk()
        coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns false
        coEvery { externalItemRepository.getExternalItemsFromUri(any(), any()) } returns listOf(fakeItem)
        coEvery { cryptoManager.encryptData(any()) } returns fakeEncryptedData
        coEvery { itemRepository.insertItems(any()) } throws Exception()
        val states = mutableListOf<State>()

        //when
        viewModel.importItemsState.observeForever { event ->
            event.getContentIfNotHandled()?.let { states.add(it) }
        }
        viewModel.setUri(uri)
        viewModel.onSlideChanged(REVIEW_ITEM_SLIDE)
        viewModel.importedItems.value?.forEach { it.selected = true }
        viewModel.onSlideChanged(SUCCESS_FAIL_SLIDE)

        //then
        TestCase.assertEquals(2, states.size)
        TestCase.assertTrue(states[0] is State.Loading)
        TestCase.assertTrue(states[1] is State.Error)
    }

    @Test
    fun `onSlideChanged with SUCCESS_FAIL_SLIDE position and no item found should emit an error`() = runBlocking {
        //given
        val uri: Uri = mockk()
        coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns false
        coEvery { externalItemRepository.getExternalItemsFromUri(any(), any()) } returns listOf()
        val states = mutableListOf<State>()

        //when
        viewModel.importItemsState.observeForever { event ->
            event.getContentIfNotHandled()?.let { states.add(it) }
        }
        viewModel.setUri(uri)
        viewModel.onSlideChanged(REVIEW_ITEM_SLIDE)
        viewModel.importedItems.value?.forEach { it.selected = false }
        viewModel.onSlideChanged(SUCCESS_FAIL_SLIDE)

        //then
        TestCase.assertEquals(2, states.size)
        TestCase.assertTrue(states[0] is State.Loading)
        TestCase.assertTrue(states[1] is State.Error)
    }

    @Test
    fun `selectAllItems should select all importedItems`() = runBlocking {
        //given
        val uri: Uri = mockk()
        coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns false
        coEvery { externalItemRepository.getExternalItemsFromUri(any(), any()) } returns listOf(fakeItem)

        //when
        viewModel.setUri(uri)
        viewModel.onSlideChanged(REVIEW_ITEM_SLIDE)
        viewModel.importedItems.value?.forEach { it.selected = false }
        viewModel.selectAllItems()

        //then
        TestCase.assertTrue(viewModel.importedItems.getOrAwaitValue().filter { it.selected }.size == 1)
    }
}
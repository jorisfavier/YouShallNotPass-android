package fr.jorisfavier.youshallnotpass

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.manager.model.EncryptedData
import fr.jorisfavier.youshallnotpass.model.ExternalItem
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.repository.ExternalItemRepository
import fr.jorisfavier.youshallnotpass.repository.ItemRepository
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.ImportItemViewModel
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.ImportItemViewModel.Companion.PASSWORD_NEEDED_SLIDE
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.ImportItemViewModel.Companion.REVIEW_ITEM_SLIDE
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.ImportItemViewModel.Companion.SUCCESS_FAIL_SLIDE
import fr.jorisfavier.youshallnotpass.utils.CoroutineDispatchers
import fr.jorisfavier.youshallnotpass.utils.State
import fr.jorisfavier.youshallnotpass.utils.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class ImportItemViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val itemRepository: ItemRepository = mockk()
    private val cryptoManager: CryptoManager = mockk()
    private val externalItemRepository: ExternalItemRepository = mockk()
    private val coroutineDispatchers = CoroutineDispatchers(
        default = Dispatchers.Main,
        io = Dispatchers.Main,
        unconfined = Dispatchers.Main,
    )
    private val viewModel = ImportItemViewModel(
        externalItemRepository,
        cryptoManager,
        itemRepository,
        coroutineDispatchers,
    )

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
        coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns Result.success(true)

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
        coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns Result.success(false)

        //when
        viewModel.setUri(uri)

        //then
        TestCase.assertFalse(viewModel.isSecureFile.value ?: true)
        TestCase.assertTrue(viewModel.isFileSelected)
        TestCase.assertEquals(Unit, viewModel.navigate.getOrAwaitValue().peekContent())
    }

    @Test
    fun `onSlideChanged with PASSWORD_NEEDED_SLIDE position should emit a navigate event if isSecureFile is false`() =
        runBlocking {
            //given
            val uri: Uri = mockk()
            coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns Result.success(
                false
            )
            var count = 0

            //when
            viewModel.navigate.observeForever {
                if (it.getContentIfNotHandled() != null) {
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
    fun `onSlideChanged with PASSWORD_NEEDED_SLIDE position should not emit a navigate event if isSecureFile is true`() =
        runBlocking {
            //given
            val uri: Uri = mockk()
            coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns Result.success(
                true
            )
            var count = 0

            //when
            viewModel.navigate.observeForever {
                if (it.getContentIfNotHandled() != null) {
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
    fun `onSlideChanged with REVIEW_ITEM_SLIDE position should load external items`() =
        runBlocking {
            //given
            val uri: Uri = mockk()
            val filePassword = slot<String>()
            coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns Result.success(
                true
            )
            coEvery {
                externalItemRepository.getExternalItemsFromUri(
                    any(),
                    capture(filePassword)
                )
            } returns Result.success(listOf(fakeItem))
            viewModel.onPasswordChanged(fakePassword)
            val states = mutableListOf<State<Unit>>()

            //when
            viewModel.loadFromUriState.observeForever { state ->
                states.add(state)
            }
            viewModel.setUri(uri)
            viewModel.onSlideChanged(REVIEW_ITEM_SLIDE)

            //then
            TestCase.assertTrue(viewModel.isSecureFile.value ?: false)
            TestCase.assertEquals(fakePassword, filePassword.captured)
            TestCase.assertEquals(2, states.size)
            TestCase.assertTrue(states[0] is State.Loading)
            TestCase.assertTrue(states[1] is State.Success)
            TestCase.assertEquals(
                fakeItem,
                viewModel.importedItems.value?.firstOrNull()?.externalItem
            )
        }

    @Test
    fun `onSlideChanged with REVIEW_ITEM_SLIDE position and empty item list should emit error`() =
        runBlocking {
            //given
            val uri: Uri = mockk()
            val filePassword = slot<String>()
            coEvery {
                externalItemRepository.isSecuredWithPassword(any())
            } returns Result.success(true)
            coEvery {
                externalItemRepository.getExternalItemsFromUri(
                    any(),
                    capture(filePassword)
                )
            } returns Result.success(listOf())
            viewModel.onPasswordChanged(fakePassword)
            val states = mutableListOf<State<Unit>>()

            //when
            viewModel.loadFromUriState.observeForever {
                states.add(it)
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
    fun `onSlideChanged with REVIEW_ITEM_SLIDE position and exception from the repository should emit an error`() =
        runBlocking {
            //given
            val uri: Uri = mockk()
            val filePassword = slot<String>()
            coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns Result.success(
                true
            )
            coEvery {
                externalItemRepository.getExternalItemsFromUri(
                    any(),
                    capture(filePassword)
                )
            } returns Result.failure(Exception())
            viewModel.onPasswordChanged(fakePassword)
            val states = mutableListOf<State<Unit>>()
            var count = 0

            //when
            viewModel.navigate.observeForever { event ->
                if (event.getContentIfNotHandled() != null) {
                    count++
                }
            }
            viewModel.loadFromUriState.observeForever {
                states.add(it)
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
    fun `onSlideChanged with SUCCESS_FAIL_SLIDE position and selected items should import items`() =
        runBlocking {
            //given
            val uri: Uri = mockk()
            val items = slot<List<Item>>()
            coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns Result.success(
                false
            )
            coEvery {
                externalItemRepository.getExternalItemsFromUri(any(), any())
            } returns Result.success(listOf(fakeItem))
            coEvery { cryptoManager.encryptData(any()) } returns Result.success(fakeEncryptedData)
            coEvery { itemRepository.insertItems(capture(items)) } returns Result.success(Unit)
            val states = mutableListOf<State<Unit>>()

            //when
            viewModel.importItemsState.observeForever {
                states.add(it)
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
            TestCase.assertEquals(
                fakeEncryptedData.initializationVector,
                items.captured.first().initializationVector
            )
            TestCase.assertEquals(fakeEncryptedData.ciphertext, items.captured.first().password)
            TestCase.assertEquals(0, items.captured.first().id)
        }

    @Test
    fun `onSlideChanged with SUCCESS_FAIL_SLIDE position and no item selected should emit an error`() =
        runBlocking {
            //given
            val uri: Uri = mockk()
            coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns Result.success(
                false
            )
            coEvery {
                externalItemRepository.getExternalItemsFromUri(any(), any())
            } returns Result.success(listOf(fakeItem))
            val states = mutableListOf<State<Unit>>()

            //when
            viewModel.importItemsState.observeForever {
                states.add(it)
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
    fun `onSlideChanged with SUCCESS_FAIL_SLIDE position and an exception from the repository should emit an error`() =
        runBlocking {
            //given
            val uri: Uri = mockk()
            coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns Result.success(
                false
            )
            coEvery {
                externalItemRepository.getExternalItemsFromUri(any(), any())
            } returns Result.success(listOf(fakeItem))
            coEvery { cryptoManager.encryptData(any()) } returns Result.success(fakeEncryptedData)
            coEvery { itemRepository.insertItems(any()) } returns Result.failure(Exception())
            val states = mutableListOf<State<Unit>>()

            //when
            viewModel.importItemsState.observeForever {
                states.add(it)
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
    fun `onSlideChanged with SUCCESS_FAIL_SLIDE position and no item found should emit an error`() =
        runBlocking {
            //given
            val uri: Uri = mockk()
            coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns Result.success(
                false
            )
            coEvery {
                externalItemRepository.getExternalItemsFromUri(
                    any(),
                    any()
                )
            } returns Result.success(listOf())
            val states = mutableListOf<State<Unit>>()

            //when
            viewModel.importItemsState.observeForever {
                states.add(it)
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
        coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns Result.success(false)
        coEvery {
            externalItemRepository.getExternalItemsFromUri(any(), any())
        } returns Result.success(listOf(fakeItem))

        //when
        viewModel.setUri(uri)
        viewModel.onSlideChanged(REVIEW_ITEM_SLIDE)
        viewModel.importedItems.value?.forEach { it.selected = false }
        viewModel.selectAllItems()

        //then
        TestCase.assertTrue(
            viewModel.importedItems.getOrAwaitValue().filter { it.selected }.size == 1
        )
    }
}

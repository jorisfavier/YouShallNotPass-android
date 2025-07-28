package fr.jorisfavier.youshallnotpass

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.manager.model.EncryptedData
import fr.jorisfavier.youshallnotpass.model.ExternalItem
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.repository.ExternalItemRepository
import fr.jorisfavier.youshallnotpass.repository.ItemRepository
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.ImportItemStep
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.ImportItemViewModel
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.review.SelectableExternalItem
import fr.jorisfavier.youshallnotpass.utils.CoroutineDispatchers
import fr.jorisfavier.youshallnotpass.utils.MainDispatcherRule
import fr.jorisfavier.youshallnotpass.utils.State
import fr.jorisfavier.youshallnotpass.utils.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ImportItemViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainDispatcherRule()

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
    fun `setUri with a ysnp file uri should emit a navigate event`() = runTest {
        //given
        val uri: Uri = mockk()
        coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns Result.success(true)

        //when
        viewModel.setUri(uri)

        //then
        TestCase.assertEquals(
            ImportItemStep.PASSWORD_NEEDED.ordinal,
            viewModel.navigate.getOrAwaitValue().peekContent(),
        )
    }

    @Test
    fun `setUri with a csv file uri should emit a navigate event`() = runTest {
        //given
        val uri: Uri = mockk()
        coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns Result.success(false)

        //when
        viewModel.setUri(uri)

        //then
        TestCase.assertEquals(
            ImportItemStep.PASSWORD_NEEDED.ordinal,
            viewModel.navigate.getOrAwaitValue().peekContent()
        )
    }

    @Test
    fun `onSlideChanged with PASSWORD_NEEDED position should emit a navigate event if isSecureFile is false`() =
        runTest {
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
            viewModel.onSlideChanged(ImportItemStep.PASSWORD_NEEDED.ordinal)

            //then
            TestCase.assertEquals(2, count)
        }

    @Test
    fun `onSlideChanged with PASSWORD_NEEDED position should not emit a navigate event if isSecureFile is true`() =
        runTest {
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
            viewModel.onSlideChanged(ImportItemStep.PASSWORD_NEEDED.ordinal)

            //then
            TestCase.assertEquals(1, count)
        }

    @Test
    fun `onSlideChanged with REVIEW_ITEM position should load external items`() =
        runTest {
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
            val states = mutableListOf<State<List<SelectableExternalItem>>>()

            //when
            viewModel.loadFromUriState.observeForever { state ->
                states.add(state)
            }
            viewModel.setUri(uri)
            viewModel.onSlideChanged(ImportItemStep.REVIEW_ITEM.ordinal)

            //then
            TestCase.assertEquals(fakePassword, filePassword.captured)
            TestCase.assertEquals(2, states.size)
            TestCase.assertTrue(states[0] is State.Loading)
            TestCase.assertTrue(states[1] is State.Success)
            TestCase.assertEquals(
                fakeItem,
                (states[1] as State.Success).value.firstOrNull()?.externalItem,
            )
        }

    @Test
    fun `onSlideChanged with REVIEW_ITEM position and empty item list should emit error`() =
        runTest {
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
            val states = mutableListOf<State<List<SelectableExternalItem>>>()

            //when
            viewModel.loadFromUriState.observeForever {
                states.add(it)
            }
            viewModel.setUri(uri)
            viewModel.onSlideChanged(ImportItemStep.REVIEW_ITEM.ordinal)

            //then
            TestCase.assertEquals(fakePassword, filePassword.captured)
            TestCase.assertEquals(2, states.size)
            TestCase.assertTrue(states[0] is State.Loading)
            TestCase.assertTrue(states[1] is State.Error)
        }

    @Test
    fun `onSlideChanged with REVIEW_ITEM position and exception from the repository should emit an error`() =
        runTest {
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
            val states = mutableListOf<State<List<SelectableExternalItem>>>()
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
            viewModel.onSlideChanged(ImportItemStep.REVIEW_ITEM.ordinal)

            //then
            TestCase.assertEquals(fakePassword, filePassword.captured)
            TestCase.assertEquals(2, states.size)
            TestCase.assertTrue(states[0] is State.Loading)
            TestCase.assertTrue(states[1] is State.Error)
            TestCase.assertEquals(2, count)
        }

    @Test
    fun `selectAllItems should select all importedItems`() = runTest {
        //given
        val uri: Uri = mockk()
        val items = slot<List<Item>>()
        coEvery { externalItemRepository.isSecuredWithPassword(any()) } returns Result.success(
            false
        )
        coEvery {
            externalItemRepository.getExternalItemsFromUri(any(), any())
        } returns Result.success(listOf(fakeItem, fakeItem.copy(title = "test2")))
        coEvery { cryptoManager.encryptData(any()) } returns Result.success(fakeEncryptedData)
        coEvery { itemRepository.insertItems(capture(items)) } returns Result.success(Unit)
        val states = mutableListOf<State<Unit>>()

        //when
        viewModel.importItemsState.observeForever {
            states.add(it)
        }
        viewModel.setUri(uri)
        viewModel.onSlideChanged(ImportItemStep.REVIEW_ITEM.ordinal)
        viewModel.selectAllItems()
        viewModel.onSlideChanged(ImportItemStep.SUCCESS_FAIL.ordinal)
        //then
        TestCase.assertTrue(items.captured.size == 2)
    }

    @Test
    fun `onSlideChanged with SUCCESS_FAIL position and selected items should import items`() =
        runTest {
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
            viewModel.onSlideChanged(ImportItemStep.REVIEW_ITEM.ordinal)
            viewModel.selectAllItems()
            viewModel.onSlideChanged(ImportItemStep.SUCCESS_FAIL.ordinal)
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
    fun `onSlideChanged with SUCCESS_FAIL position and no item selected should emit an error`() =
        runTest {
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
            viewModel.onSlideChanged(ImportItemStep.REVIEW_ITEM.ordinal)
            viewModel.onSlideChanged(ImportItemStep.SUCCESS_FAIL.ordinal)

            //then
            TestCase.assertEquals(2, states.size)
            TestCase.assertTrue(states[0] is State.Loading)
            TestCase.assertTrue(states[1] is State.Error)
        }

    @Test
    fun `onSlideChanged with SUCCESS_FAIL position and an exception from the repository should emit an error`() =
        runTest {
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
            viewModel.onSlideChanged(ImportItemStep.REVIEW_ITEM.ordinal)
            viewModel.selectAllItems()
            viewModel.onSlideChanged(ImportItemStep.SUCCESS_FAIL.ordinal)

            //then
            TestCase.assertEquals(2, states.size)
            TestCase.assertTrue(states[0] is State.Loading)
            TestCase.assertTrue(states[1] is State.Error)
        }

    @Test
    fun `onSlideChanged with SUCCESS_FAIL position and no item found should emit an error`() =
        runTest {
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
            viewModel.onSlideChanged(ImportItemStep.REVIEW_ITEM.ordinal)
            viewModel.onSlideChanged(ImportItemStep.SUCCESS_FAIL.ordinal)
            //then
            TestCase.assertEquals(2, states.size)
            TestCase.assertTrue(states[0] is State.Loading)
            TestCase.assertTrue(states[1] is State.Error)
        }
}

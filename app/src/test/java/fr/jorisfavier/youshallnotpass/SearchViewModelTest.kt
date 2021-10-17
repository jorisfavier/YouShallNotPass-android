package fr.jorisfavier.youshallnotpass

import android.content.ClipData
import android.content.ClipboardManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.ItemDataType
import fr.jorisfavier.youshallnotpass.repository.DesktopRepository
import fr.jorisfavier.youshallnotpass.repository.ItemRepository
import fr.jorisfavier.youshallnotpass.ui.search.SearchViewModel
import fr.jorisfavier.youshallnotpass.utils.State
import fr.jorisfavier.youshallnotpass.utils.getOrAwaitValue
import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {
    private val itemRepository: ItemRepository = mockk()
    private val cryptoManager: CryptoManager = mockk()
    private val clipboardManager: ClipboardManager = mockk()
    private val appPreferences: AppPreferenceDataSource = mockk()
    private val desktopRepository: DesktopRepository = mockk()

    private val viewModel by lazy {
        SearchViewModel(
            itemRepository,
            cryptoManager,
            clipboardManager,
            appPreferences,
            desktopRepository,
            debounceDurationMs = 0,
        )
    }

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val fakeItem = Item(
        id = 1,
        title = "Test title",
        login = "test login",
        password = ByteArray(0),
        initializationVector = ByteArray(0)
    )
    private val fakePassword = "fake password"

    @Test
    fun `on first launch without item result should be empty with an explanation message`() =
        runBlocking {
            //given
            every { appPreferences.observeShouldHideItems() } returns flow {
                emit(false)
            }
            coEvery { itemRepository.getAllItems() } returns listOf()

            //when
            viewModel.search.value = ""
            viewModel.results.observeForever {}
            viewModel.hasNoResult.observeForever {}
            viewModel.noResultTextIdRes.observeForever {}

            //then
            val results = viewModel.results.value
            assertEquals(true, viewModel.hasNoResult.value)
            assertTrue(results is State.Success && results.value.isEmpty())
            assertEquals(R.string.no_item_yet, viewModel.noResultTextIdRes.value)
        }

    @Test
    fun `on first launch without item with hidden items, result should be empty with an explanation message`() {
        //given
        every { appPreferences.observeShouldHideItems() } returns flow {
            emit(true)
        }
        coEvery { itemRepository.getAllItems() } returns listOf()

        //when
        viewModel.hasNoResult.observeForever {}
        viewModel.noResultTextIdRes.observeForever {}
        viewModel.results.observeForever {}

        //then
        val results = viewModel.results.value
        assertEquals(true, viewModel.hasNoResult.value)
        assertTrue(results is State.Success && results.value.isEmpty())
        assertEquals(R.string.use_the_search, viewModel.noResultTextIdRes.value)
    }

    @Test
    fun `on first launch when repository throws an exception we should emit an empty list of items`() {
        //given
        every { appPreferences.observeShouldHideItems() } returns flow {
            emit(false)
        }
        coEvery { itemRepository.getAllItems() } throws Exception()

        //when
        viewModel.hasNoResult.observeForever {}
        viewModel.noResultTextIdRes.observeForever {}
        viewModel.results.observeForever {}

        //then
        val results = viewModel.results.value
        assertEquals(true, viewModel.hasNoResult.value)
        assertTrue(results is State.Success && results.value.isEmpty())
    }

    @Test
    fun `on search when no item found we should have a no result message`() {
        //given
        every { appPreferences.observeShouldHideItems() } returns flow {
            emit(true)
        }
        coEvery { itemRepository.getAllItems() } returns listOf()
        coEvery { itemRepository.searchItem(any()) } returns listOf()

        //when
        viewModel.hasNoResult.observeForever {}
        viewModel.noResultTextIdRes.observeForever {}
        viewModel.results.observeForever {}
        viewModel.search.value = "test"

        //then
        val results = viewModel.results.value
        assertEquals(true, viewModel.hasNoResult.value)
        assertTrue(results is State.Success && results.value.isEmpty())
        assertEquals(R.string.no_results_found, viewModel.noResultTextIdRes.value)
    }

    @Test
    fun `on search when items are found we should emit items`() {
        //given
        every { appPreferences.observeShouldHideItems() } returns flow {
            emit(true)
        }
        coEvery { itemRepository.getAllItems() } returns listOf()
        coEvery { itemRepository.searchItem(any()) } returns listOf(fakeItem)

        //when
        viewModel.hasNoResult.observeForever {}
        viewModel.noResultTextIdRes.observeForever {}
        viewModel.search.value = "test"

        //then
        val results = viewModel.results.getOrAwaitValue()
        assertEquals(false, viewModel.hasNoResult.value)
        assertTrue(results is State.Success && results.value.isNotEmpty())
    }

    @Test
    fun `on search when repository throws an exception we should emit an empty list of items`() {
        //given
        every { appPreferences.observeShouldHideItems() } returns flow {
            emit(false)
        }
        coEvery { itemRepository.getAllItems() } returns listOf(fakeItem)
        coEvery { itemRepository.searchItem(any()) } throws Exception()

        //when
        viewModel.hasNoResult.observeForever {}
        viewModel.noResultTextIdRes.observeForever {}
        viewModel.search.value = "test"

        //then
        val results = viewModel.results.getOrAwaitValue()
        assertEquals(true, viewModel.hasNoResult.value)
        assertTrue(results is State.Success && results.value.isEmpty())
    }

    @Test
    fun `when changing HIDE_ITEMS_PREFERENCE_KEY shared preference we should emit items`() {
        //given
        every { appPreferences.observeShouldHideItems() } returns flow {
            emit(true)
            emit(false)
        }
        coEvery { itemRepository.getAllItems() } returns listOf(fakeItem)

        //when
        viewModel.results.observeForever { }
        viewModel.refreshItems()

        //then
        val results = viewModel.results.value
        assertTrue(results is State.Success && results.value.isNotEmpty())
    }

    @Test
    fun `when refreshItems called we should emit items`() {
        //given
        every { appPreferences.observeShouldHideItems() } returns flow {
            emit(false)
        }
        coEvery { itemRepository.getAllItems() } returnsMany listOf(listOf(), listOf(fakeItem))

        //when
        viewModel.results.observeForever { }
        viewModel.refreshItems()

        //then
        val results = viewModel.results.value
        assertTrue(results is State.Success && results.value.isNotEmpty())
    }

    @Test
    fun `when copyToClipboard called with type LOGIN we copy the login to the clipboard`() {
        //given
        val slot = slot<String>()
        mockkStatic(ClipData::class)
        every { appPreferences.observeShouldHideItems() } returns flow {
            emit(false)
        }
        every { cryptoManager.decryptData(any(), any()) } returns fakePassword
        every { clipboardManager.setPrimaryClip(any()) } just runs
        every { ClipData.newPlainText(any(), capture(slot)) } returns mockk()
        //when
        val result = viewModel.copyToClipboard(fakeItem, ItemDataType.LOGIN)

        //then
        assertEquals(fakeItem.login, slot.captured)
        assertTrue(result.isSuccess)
        assertEquals(R.string.copy_login_to_clipboard_success, result.getOrNull())
    }

    @Test
    fun `when copyToClipboard called with type PASSWORD we copy the login to the clipboard`() {
        //given
        val slot = slot<String>()
        mockkStatic(ClipData::class)
        every { appPreferences.observeShouldHideItems() } returns flow {
            emit(false)
        }
        every { cryptoManager.decryptData(any(), any()) } returns fakePassword
        every { clipboardManager.setPrimaryClip(any()) } just runs
        every { ClipData.newPlainText(any(), capture(slot)) } returns mockk()
        //when
        val result = viewModel.copyToClipboard(fakeItem, ItemDataType.PASSWORD)

        //then
        assertEquals(fakePassword, slot.captured)
        assertTrue(result.isSuccess)
        assertEquals(R.string.copy_password_to_clipboard_success, result.getOrNull())
    }

    @Test
    fun `when copyToClipboard called and cryptoManager throws an exception then nothing should be copied to the clipboard`() {
        //given
        every { appPreferences.observeShouldHideItems() } returns flow {
            emit(false)
        }
        every { cryptoManager.decryptData(any(), any()) } throws Exception()
        every { clipboardManager.setPrimaryClip(any()) } just runs
        //when
        val result = viewModel.copyToClipboard(fakeItem, ItemDataType.PASSWORD)

        //then

        assertTrue(result.isFailure)
        verify(inverse = true) { clipboardManager.setPrimaryClip(any()) }
    }

    @Test
    fun `when deleteItem called the item should be removed from the repository and the flow should emit a success`() =
        runBlocking {
            //given
            every { appPreferences.observeShouldHideItems() } returns flow {
                emit(false)
            }
            coEvery { itemRepository.deleteItem(any()) } just runs
            //when
            val result = viewModel.deleteItem(fakeItem).first()

            //then
            assertTrue(result.isSuccess)
        }

    @Test
    fun `when deleteItem called and the repository throws an exception the flow should emit a failure`() =
        runBlocking {
            //given
            every { appPreferences.observeShouldHideItems() } returns flow {
                emit(false)
            }
            coEvery { itemRepository.deleteItem(any()) } throws Exception()
            //when
            val result = viewModel.deleteItem(fakeItem).first()

            //then
            assertTrue(result.isFailure)
        }

    @Test
    fun `when sendToDesktop called with type LOGIN we send the login info to the desktop app`() =
        runBlocking {
            //given
            every { appPreferences.observeShouldHideItems() } returns flow {
                emit(false)
            }
            val slot = slot<String>()
            coEvery { desktopRepository.sendData(capture(slot)) } just runs
            //when
            val result = viewModel.sendToDesktop(fakeItem, ItemDataType.LOGIN).first()

            //then
            assertTrue(result.isSuccess)
            assertEquals(fakeItem.login, slot.captured)
        }

    @Test
    fun `when sendToDesktop called with type PASSWORD we send the password info to the desktop app`() =
        runBlocking {
            //given
            every { appPreferences.observeShouldHideItems() } returns flow {
                emit(false)
            }
            val slot = slot<String>()
            coEvery { desktopRepository.sendData(capture(slot)) } just runs
            every { cryptoManager.decryptData(any(), any()) } returns fakePassword
            //when
            val result = viewModel.sendToDesktop(fakeItem, ItemDataType.PASSWORD).first()

            //then
            assertTrue(result.isSuccess)
            assertEquals(fakePassword, slot.captured)
        }

    @Test
    fun `when sendToDesktop called and cryptoManager throws an exception then nothing should be sent to the desktop app`() =
        runBlocking {
            //given
            every { appPreferences.observeShouldHideItems() } returns flow {
                emit(false)
            }
            coEvery { desktopRepository.sendData(any()) } just runs
            every { cryptoManager.decryptData(any(), any()) } throws Exception()
            //when
            val result = viewModel.sendToDesktop(fakeItem, ItemDataType.PASSWORD).first()

            //then
            assertTrue(result.isFailure)
            coVerify(inverse = true) { desktopRepository.sendData(any()) }

        }

    @Test
    fun `when sendToDesktop called and desktopRepository throws an exception then the flow should emit a failure`() =
        runBlocking {
            //given
            every { appPreferences.observeShouldHideItems() } returns flow {
                emit(false)
            }
            coEvery { desktopRepository.sendData(any()) } throws Exception()
            //when
            val result = viewModel.sendToDesktop(fakeItem, ItemDataType.LOGIN).first()

            //then
            assertTrue(result.isFailure)
        }


}
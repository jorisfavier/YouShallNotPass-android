package fr.jorisfavier.youshallnotpass

import android.content.pm.PackageManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.model.AutofillParsedStructure
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.repository.ItemRepository
import fr.jorisfavier.youshallnotpass.ui.autofill.AutofillDataSetInfo
import fr.jorisfavier.youshallnotpass.ui.autofill.AutofillSearchViewModel
import fr.jorisfavier.youshallnotpass.utils.AssistStructureUtil
import fr.jorisfavier.youshallnotpass.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description



class AutofillSearchViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainDispatcherRule()

    private val itemRepository: ItemRepository = mockk()
    private val cryptoManager: CryptoManager = mockk()
    private val packageManager: PackageManager = mockk()

    private val viewModel by lazy {
        AutofillSearchViewModel(
            itemRepository = itemRepository,
            cryptoManager = cryptoManager,
            packageManager = packageManager,
        )
    }
    private val fakeAppName = "fake"
    private val fakePassword = "fake password"
    private val fakeParsedStructure = AutofillParsedStructure(
        webDomain = null,
        appName = "fake",
        certificatesHashes = listOf("fake certificate"),
        items = emptyList(),
        ignoreIds = emptyList(),
        isNewCredentials = false,
    )
    private val fakeItem = Item(
        id = 1,
        title = "Test title",
        login = "test login",
        password = ByteArray(0),
        initializationVector = ByteArray(0)
    )

    @Test
    fun `on first launch we should search by certificates and then by app name`() = runTest {
        //given
        mockkStatic(AssistStructureUtil::class)
        every {
            AssistStructureUtil.traverseStructure(
                any(),
                any()
            )
        } returns fakeParsedStructure
        coEvery { itemRepository.searchItemByCertificates(any()) } returns Result.success(emptyList())
        coEvery { itemRepository.searchItem(any()) } returns Result.success(emptyList())

        //when
        viewModel.results.observeForever {}
        viewModel.setAutofillInfos(mockk(), mockk())

        //then
        coVerify { itemRepository.searchItemByCertificates(any()) }
        coVerify { itemRepository.searchItem("%$fakeAppName%") }
    }

    @Test
    fun `when onItemClicked we should update the certificates and send the autofill information`() =
        runTest {
            //given
            mockkStatic(AssistStructureUtil::class)
            val result = mutableListOf<AutofillDataSetInfo>()
            val slot = slot<Item>()
            every {
                AssistStructureUtil.traverseStructure(
                    any(),
                    any()
                )
            } returns fakeParsedStructure
            coEvery { itemRepository.updateOrCreateItem(capture(slot)) } returns Result.success(Unit)
            coEvery { cryptoManager.decryptData(any(), any()) } returns Result.success(fakePassword)

            //when
            viewModel.setAutofillInfos(mockk(), mockk())
            viewModel.onItemClicked(fakeItem)
            viewModel.autofillResponse.observeForever {
                result.add(it.peekContent())
            }

            //then
            TestCase.assertEquals(
                fakeParsedStructure.certificatesHashes,
                slot.captured.packageCertificate
            )
            TestCase.assertTrue(result.isNotEmpty())
            TestCase.assertEquals(fakePassword, result.first().itemPassword)
        }

    @Test
    fun `when onItemClicked and updateOrCreateItem throws an exception should not prevent emitting autofillResponse`() =
        runTest {
            //given
            mockkStatic(AssistStructureUtil::class)
            val result = mutableListOf<AutofillDataSetInfo>()
            every {
                AssistStructureUtil.traverseStructure(
                    any(),
                    any()
                )
            } returns fakeParsedStructure
            coEvery { itemRepository.updateOrCreateItem(any()) } returns Result.failure(Exception())
            coEvery { cryptoManager.decryptData(any(), any()) } returns Result.success(fakePassword)

            //when
            viewModel.setAutofillInfos(mockk(), mockk())
            viewModel.onItemClicked(fakeItem)
            viewModel.autofillResponse.observeForever {
                result.add(it.peekContent())
            }

            //then
            TestCase.assertTrue(result.isNotEmpty())
            TestCase.assertEquals(fakePassword, result.first().itemPassword)
        }
}

package fr.jorisfavier.youshallnotpass

import android.content.pm.PackageManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.model.AutofillParsedStructure
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.ui.autofill.AutofillDataSetInfo
import fr.jorisfavier.youshallnotpass.ui.autofill.AutofillSearchViewModel
import fr.jorisfavier.youshallnotpass.utils.AssistStructureUtil
import io.mockk.*
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test

class AutofillSearchViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val itemRepository: IItemRepository = mockk()
    private val cryptoManager: ICryptoManager = mockk()
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
    fun `on first launch we should search by certificates and then by app name`() {
        //given
        mockkStatic(AssistStructureUtil::class)
        every {
            AssistStructureUtil.traverseStructure(
                any(),
                any()
            )
        } returns fakeParsedStructure
        coEvery { itemRepository.searchItemByCertificates(any()) } returns emptyList()
        coEvery { itemRepository.searchItem(any()) } returns emptyList()

        //when
        viewModel.results.observeForever {}
        viewModel.setAutofillInfos(mockk(), mockk())

        //then
        coVerify { itemRepository.searchItemByCertificates(any()) }
        coVerify { itemRepository.searchItem("%$fakeAppName%") }
    }

    @Test
    fun `when onItemClicked we should update the certificates and send the autofill information`() {
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
        coEvery { itemRepository.updateOrCreateItem(capture(slot)) } just runs
        coEvery { cryptoManager.decryptData(any(), any()) } returns fakePassword

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
    fun `when onItemClicked and updateOrCreateItem throws an exception should not prevent emitting autofillResponse`() {
        //given
        mockkStatic(AssistStructureUtil::class)
        val result = mutableListOf<AutofillDataSetInfo>()
        every {
            AssistStructureUtil.traverseStructure(
                any(),
                any()
            )
        } returns fakeParsedStructure
        coEvery { itemRepository.updateOrCreateItem(any()) } throws Exception()
        coEvery { cryptoManager.decryptData(any(), any()) } returns fakePassword

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
package fr.jorisfavier.youshallnotpass

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.manager.model.EncryptedData
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.exception.YsnpException
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.ui.item.ItemEditViewModel
import fr.jorisfavier.youshallnotpass.utils.PasswordUtil
import fr.jorisfavier.youshallnotpass.utils.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class ItemEditViewModelTest {

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

    private val fakeDecryptedPassword = "fake password"

    private val fakeEncryptedData
        get() = EncryptedData(fakeItem.password, fakeItem.initializationVector)
    private val newFakeTitle = "new fake title"
    private val cryptoManager: ICryptoManager = mockk()
    private val itemRepo: IItemRepository = mockk()
    private val viewModel = ItemEditViewModel(cryptoManager, itemRepo)

    @Test
    fun `on initData ItemEditViewModel should have numbers, symbol, uppercase, a password default size and 'create' button text`() {
        //given
        //when
        viewModel.initData(0)

        //then
        assertEquals(true, viewModel.hasNumber.getOrAwaitValue())
        assertEquals(true, viewModel.hasSymbol.getOrAwaitValue())
        assertEquals(true, viewModel.hasUppercase.getOrAwaitValue())
        assertEquals(R.string.item_create, viewModel.createOrUpdateText.getOrAwaitValue())
        assertEquals(PasswordUtil.MINIMUM_SECURE_SIZE, viewModel.passwordLengthValue.getOrAwaitValue())
    }

    @Test
    fun `on initData with a valid item id ItemEditViewModel should emit an 'update' button text, an item login and an item password`() {
        //given
        coEvery { itemRepo.getItemById(1) } returns fakeItem
        every { cryptoManager.decryptData(fakeItem.password, fakeItem.initializationVector) } returns fakeDecryptedPassword

        val viewModel = ItemEditViewModel(cryptoManager, itemRepo)

        //when
        viewModel.initData(1)

        //then
        assertEquals(R.string.item_update, viewModel.createOrUpdateText.getOrAwaitValue())
        assertEquals(fakeItem.login, viewModel.login.getOrAwaitValue())
        assertEquals(fakeDecryptedPassword, viewModel.password.getOrAwaitValue())
    }

    @Test
    fun `generateSecurePassword with symbol disabled should emit a password without symbols`() {
        //given
        viewModel.hasSymbol.value = false

        //when

        //force passwordLengthValue to emit values
        viewModel.passwordLengthValue.observeForever {}
        viewModel.generateSecurePassword()

        //then
        val password = viewModel.password.getOrAwaitValue()
        assertTrue(password.filter { PasswordUtil.SYMBOLS.contains(it) }.toList().isEmpty())
        assertTrue(password.filter { PasswordUtil.UPPERCASE.contains(it) }.toList().isNotEmpty())
        assertTrue(password.filter { PasswordUtil.NUMBERS.contains(it) }.toList().isNotEmpty())
        assertTrue(password.length == PasswordUtil.MINIMUM_SECURE_SIZE)

    }

    @Test
    fun `generateSecurePassword with number disabled should emit a password without numbers`() {
        //given
        viewModel.hasNumber.value = false

        //when

        //force passwordLengthValue to emit values
        viewModel.passwordLengthValue.observeForever {}
        viewModel.generateSecurePassword()

        //then
        val password = viewModel.password.getOrAwaitValue()
        assertTrue(password.filter { PasswordUtil.NUMBERS.contains(it) }.toList().isEmpty())
        assertTrue(password.filter { PasswordUtil.UPPERCASE.contains(it) }.toList().isNotEmpty())
        assertTrue(password.filter { PasswordUtil.SYMBOLS.contains(it) }.toList().isNotEmpty())
        assertTrue(password.length == PasswordUtil.MINIMUM_SECURE_SIZE)
    }

    @Test
    fun `generateSecurePassword with uppercase disabled should emit a password without uppercase`() {
        //given
        viewModel.hasUppercase.value = false

        //when
        //force passwordLengthValue to emit values
        viewModel.passwordLengthValue.observeForever {}
        viewModel.generateSecurePassword()

        //then
        val password = viewModel.password.getOrAwaitValue()
        assertTrue(password.filter { PasswordUtil.UPPERCASE.contains(it) }.toList().isEmpty())
        assertTrue(password.filter { PasswordUtil.SYMBOLS.contains(it) }.toList().isNotEmpty())
        assertTrue(password.filter { PasswordUtil.NUMBERS.contains(it) }.toList().isNotEmpty())
        assertTrue(password.length == PasswordUtil.MINIMUM_SECURE_SIZE)
    }

    @Test
    fun `generateSecurePassword with passwordLength changed should emit a password with the correct size`() {
        //given
        val passwordLength = 3
        viewModel.passwordLength.value = passwordLength

        //when

        //force passwordLengthValue to emit values
        viewModel.passwordLengthValue.observeForever {}
        viewModel.generateSecurePassword()

        //then
        val password = viewModel.password.getOrAwaitValue()
        assertTrue(password.length == (passwordLength + PasswordUtil.MINIMUM_SECURE_SIZE))
        assertTrue(password.filter { PasswordUtil.UPPERCASE.contains(it) }.toList().isNotEmpty())
        assertTrue(password.filter { PasswordUtil.NUMBERS.contains(it) }.toList().isNotEmpty())
        assertTrue(password.filter { PasswordUtil.SYMBOLS.contains(it) }.toList().isNotEmpty())
    }

    @Test
    fun `updateOrCreateItem should return a success when password and name are provided`() = runBlocking {
        //given
        every { cryptoManager.encryptData(fakeDecryptedPassword) } returns fakeEncryptedData
        coEvery { itemRepo.searchItem(fakeItem.title) } returns listOf()
        coEvery { itemRepo.updateOrCreateItem(any()) } just runs

        //when
        viewModel.initData(0)
        viewModel.password.value = fakeDecryptedPassword
        viewModel.name.value = fakeItem.title
        val result = viewModel.updateOrCreateItem().first()

        //then
        assertTrue(result.isSuccess)
        assertEquals(R.string.item_creation_success, result.getOrNull())
    }

    @Test
    fun `updateOrCreateItem should return an error when password and name are not provided`() = runBlocking {
        //given
        every { cryptoManager.encryptData(fakeDecryptedPassword) } returns fakeEncryptedData
        coEvery { itemRepo.searchItem(fakeItem.title) } returns listOf()
        coEvery { itemRepo.updateOrCreateItem(any()) } just runs

        //when
        viewModel.initData(0)
        val result = viewModel.updateOrCreateItem().first()

        //then
        assertTrue(result.isFailure)
        assertEquals(R.string.item_name_or_password_missing, (result.exceptionOrNull() as? YsnpException)?.messageResId)
    }

    @Test
    fun `updateOrCreateItem should return an error when trying to add an item with a same name`() = runBlocking {
        //given
        every { cryptoManager.encryptData(fakeDecryptedPassword) } returns fakeEncryptedData
        coEvery { itemRepo.searchItem(fakeItem.title) } returns listOf(fakeItem)
        coEvery { itemRepo.updateOrCreateItem(any()) } just runs

        //when
        viewModel.initData(0)
        viewModel.password.value = fakeDecryptedPassword
        viewModel.name.value = fakeItem.title
        val result = viewModel.updateOrCreateItem().first()

        //then
        assertTrue(result.isFailure)
        assertEquals(R.string.item_already_exist, (result.exceptionOrNull() as? YsnpException)?.messageResId)
    }

    @Test
    fun `updateOrCreateItem should return an error when an exception is raised`() = runBlocking {
        //given
        every { cryptoManager.encryptData(fakeDecryptedPassword) } returns fakeEncryptedData
        coEvery { itemRepo.searchItem(fakeItem.title) } returns listOf()
        coEvery { itemRepo.updateOrCreateItem(any()) } throws Exception()

        //when
        viewModel.initData(0)
        viewModel.password.value = fakeDecryptedPassword
        viewModel.name.value = fakeItem.title
        val result = viewModel.updateOrCreateItem().first()

        //then
        assertTrue(result.isFailure)
        assertEquals(R.string.error_occurred, (result.exceptionOrNull() as? YsnpException)?.messageResId)
    }

    @Test
    fun `updateOrCreateItem should return a success when updating an Item`() = runBlocking {
        //given
        val slot = slot<Item>()
        coEvery { itemRepo.getItemById(1) } returns fakeItem
        every { cryptoManager.encryptData(fakeDecryptedPassword) } returns fakeEncryptedData
        coEvery { itemRepo.updateOrCreateItem(capture(slot)) } just runs
        every { cryptoManager.decryptData(fakeItem.password, fakeItem.initializationVector) } returns fakeDecryptedPassword

        //when
        viewModel.initData(1)
        viewModel.name.value = newFakeTitle
        val result = viewModel.updateOrCreateItem().first()

        //then
        assertTrue(result.isSuccess)
        assertEquals(R.string.item_update_success, result.getOrNull())
        assertEquals(newFakeTitle.capitalize(), slot.captured.title)
    }


}
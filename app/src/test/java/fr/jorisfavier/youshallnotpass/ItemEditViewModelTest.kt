package fr.jorisfavier.youshallnotpass

import android.content.ClipboardManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.manager.model.EncryptedData
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.exception.YsnpException
import fr.jorisfavier.youshallnotpass.repository.ItemRepository
import fr.jorisfavier.youshallnotpass.ui.item.ItemEditViewModel
import fr.jorisfavier.youshallnotpass.utils.MainDispatcherRule
import fr.jorisfavier.youshallnotpass.utils.PasswordUtil
import fr.jorisfavier.youshallnotpass.utils.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ItemEditViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainDispatcherRule()

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
    private val cryptoManager: CryptoManager = mockk()
    private val itemRepo: ItemRepository = mockk()
    private val clipManager: ClipboardManager = mockk()

    @Test
    fun `on initData ItemEditViewModel should have a password default size and a 'create' button text`() =
        runTest {
            //given
            val vm = buildViewModel()
            //when
            vm.initData(0)

            //then
            assertEquals(R.string.item_create, vm.createOrUpdateText.getOrAwaitValue())
            assertEquals(
                PasswordUtil.MINIMUM_SECURE_SIZE,
                vm.passwordLength.getOrAwaitValue()
            )
        }

    @Test
    fun `on initData with a valid item id ItemEditViewModel should emit an 'update' button text, an item login and an item password`() =
        runTest {
            //given
            coEvery { itemRepo.getItemById(1) } returns Result.success(fakeItem)
            coEvery {
                cryptoManager.decryptData(
                    fakeItem.password,
                    fakeItem.initializationVector
                )
            } returns Result.success(fakeDecryptedPassword)

            val viewModel = ItemEditViewModel(
                cryptoManager = cryptoManager,
                itemRepository = itemRepo,
                clipboardManager = clipManager
            )

            //when
            viewModel.initData(1)

            //then
            assertEquals(R.string.item_update, viewModel.createOrUpdateText.getOrAwaitValue())
            assertEquals(fakeItem.login, viewModel.currentItem.getOrAwaitValue()?.login)
            assertEquals(fakeDecryptedPassword, viewModel.password.getOrAwaitValue())
        }

    @Test
    fun `on initData should init as a normal item creation when an exception is raised by the itemRepository`() =
        runTest {
            //given
            coEvery { itemRepo.getItemById(1) } returns Result.failure(Exception())
            coEvery {
                cryptoManager.decryptData(
                    fakeItem.password,
                    fakeItem.initializationVector
                )
            } returns Result.success(fakeDecryptedPassword)

            val viewModel = ItemEditViewModel(
                cryptoManager = cryptoManager,
                itemRepository = itemRepo,
                clipboardManager = clipManager
            )

            //when
            viewModel.initData(1)

            //then
            assertEquals(R.string.item_create, viewModel.createOrUpdateText.getOrAwaitValue())
        }

    @Test
    fun `generateSecurePassword with symbol disabled should emit a password without symbols`() =
        runTest {
            //given
            val viewModel = buildViewModel()

            //when
            val password = viewModel.generateSecurePassword(
                hasUppercase = true,
                hasSymbol = false,
                hasNumber = true,
            )

            //then
            assertTrue(password.filter { PasswordUtil.SYMBOLS.contains(it) }.toList().isEmpty())
            assertTrue(
                password.filter { PasswordUtil.UPPERCASE.contains(it) }.toList().isNotEmpty()
            )
            assertTrue(password.filter { PasswordUtil.NUMBERS.contains(it) }.toList().isNotEmpty())
            assertTrue(password.length == PasswordUtil.MINIMUM_SECURE_SIZE)

        }

    @Test
    fun `generateSecurePassword with number disabled should emit a password without numbers`() =
        runTest {
            //given
            val viewModel = buildViewModel()

            //when
            val password = viewModel.generateSecurePassword(
                hasUppercase = true,
                hasSymbol = true,
                hasNumber = false,
            )

            //then
            assertTrue(password.filter { PasswordUtil.NUMBERS.contains(it) }.toList().isEmpty())
            assertTrue(
                password.filter { PasswordUtil.UPPERCASE.contains(it) }.toList().isNotEmpty()
            )
            assertTrue(password.filter { PasswordUtil.SYMBOLS.contains(it) }.toList().isNotEmpty())
            assertTrue(password.length == PasswordUtil.MINIMUM_SECURE_SIZE)
        }

    @Test
    fun `generateSecurePassword with uppercase disabled should emit a password without uppercase`() =
        runTest {
            val viewModel = buildViewModel()

            //when
            val password = viewModel.generateSecurePassword(
                hasUppercase = false,
                hasSymbol = true,
                hasNumber = true,
            )

            //then
            assertTrue(password.filter { PasswordUtil.UPPERCASE.contains(it) }.toList().isEmpty())
            assertTrue(password.filter { PasswordUtil.SYMBOLS.contains(it) }.toList().isNotEmpty())
            assertTrue(password.filter { PasswordUtil.NUMBERS.contains(it) }.toList().isNotEmpty())
            assertTrue(password.length == PasswordUtil.MINIMUM_SECURE_SIZE)
        }

    @Test
    fun `generateSecurePassword with passwordLength changed should emit a password with the correct size`() =
        runTest {
            //given
            val viewModel = buildViewModel()
            val passwordLength = 3
            viewModel.onPasswordLengthChanged(passwordLength)

            //when
            val password = viewModel.generateSecurePassword(
                hasUppercase = true,
                hasNumber = true,
                hasSymbol = true,
            )

            //then
            assertTrue(password.length == (passwordLength + PasswordUtil.MINIMUM_SECURE_SIZE))
            assertTrue(
                password.filter { PasswordUtil.UPPERCASE.contains(it) }.toList().isNotEmpty()
            )
            assertTrue(password.filter { PasswordUtil.NUMBERS.contains(it) }.toList().isNotEmpty())
            assertTrue(password.filter { PasswordUtil.SYMBOLS.contains(it) }.toList().isNotEmpty())
        }

    @Test
    fun `updateOrCreateItem should return a success when password and name are provided`() =
        runTest {
            //given
            val viewModel = buildViewModel()
            coEvery { cryptoManager.encryptData(fakeDecryptedPassword) } returns Result.success(
                fakeEncryptedData
            )
            coEvery { itemRepo.searchItem(fakeItem.title) } returns Result.success(listOf())
            coEvery { itemRepo.updateOrCreateItem(any()) } returns Result.success(Unit)
            every { clipManager.setPrimaryClip(any()) } just runs

            //when
            viewModel.initData(0)
            val result = viewModel.updateOrCreateItem(
                name = fakeItem.title,
                password = fakeDecryptedPassword,
                login = null,
            ).first()

            //then
            assertTrue(result.isSuccess)
            verify { clipManager.setPrimaryClip(any()) }
            assertEquals(R.string.item_creation_success, result.getOrNull())
        }

    @Test
    fun `updateOrCreateItem should return an error when password and name are not provided`() =
        runTest {
            //given
            val viewModel = buildViewModel()
            coEvery { cryptoManager.encryptData(fakeDecryptedPassword) } returns Result.success(
                fakeEncryptedData
            )
            coEvery { itemRepo.searchItem(fakeItem.title) } returns Result.success(listOf())
            coEvery { itemRepo.updateOrCreateItem(any()) } returns Result.success(Unit)

            //when
            viewModel.initData(0)
            val result = viewModel.updateOrCreateItem(
                name = null,
                password = null,
                login = null,
            ).first()

            //then
            assertTrue(result.isFailure)
            assertEquals(
                R.string.item_name_or_password_missing,
                (result.exceptionOrNull() as? YsnpException)?.messageResId
            )
        }

    @Test
    fun `updateOrCreateItem should return an error when trying to add an item with a same name`() =
        runTest {
            //given
            val viewModel = buildViewModel()
            coEvery { cryptoManager.encryptData(fakeDecryptedPassword) } returns Result.success(
                fakeEncryptedData
            )
            coEvery { itemRepo.searchItem(fakeItem.title) } returns Result.success(listOf(fakeItem))
            coEvery { itemRepo.updateOrCreateItem(any()) } returns Result.success(Unit)

            //when
            viewModel.initData(0)
            val result = viewModel.updateOrCreateItem(
                name = fakeItem.title,
                password = fakeDecryptedPassword,
                login = null,
            ).first()

            //then
            assertTrue(result.isFailure)
            assertEquals(
                R.string.item_already_exist,
                (result.exceptionOrNull() as? YsnpException)?.messageResId
            )
        }

    @Test
    fun `updateOrCreateItem should return an error when an exception is raised`() = runTest {
        //given
        val viewModel = buildViewModel()
        coEvery { cryptoManager.encryptData(fakeDecryptedPassword) } returns Result.success(
            fakeEncryptedData
        )
        coEvery { itemRepo.searchItem(fakeItem.title) } returns Result.success(listOf())
        coEvery { itemRepo.updateOrCreateItem(any()) } returns Result.failure(Exception())

        //when
        viewModel.initData(0)
        val result = viewModel.updateOrCreateItem(
            name = fakeItem.title,
            password = fakeDecryptedPassword,
            login = null,
        ).first()

        //then
        assertTrue(result.isFailure)
        assertEquals(
            R.string.error_occurred,
            (result.exceptionOrNull() as? YsnpException)?.messageResId
        )
    }

    @Test
    fun `updateOrCreateItem should return a success when updating an Item`() = runTest {
        //given
        val viewModel = buildViewModel()
        val slot = slot<Item>()
        coEvery { itemRepo.getItemById(1) } returns Result.success(fakeItem)
        coEvery { cryptoManager.encryptData(fakeDecryptedPassword) } returns Result.success(
            fakeEncryptedData
        )
        coEvery { itemRepo.updateOrCreateItem(capture(slot)) } returns Result.success(Unit)
        coEvery {
            cryptoManager.decryptData(
                fakeItem.password,
                fakeItem.initializationVector
            )
        } returns Result.success(fakeDecryptedPassword)

        //when
        viewModel.initData(1)
        val result = viewModel.updateOrCreateItem(
            name = newFakeTitle,
            password = fakeDecryptedPassword,
            login = fakeItem.login,
        ).first()

        //then
        assertTrue(result.isSuccess)
        assertEquals(R.string.item_update_success, result.getOrNull())
        assertEquals(newFakeTitle.capitalize(), slot.captured.title)
    }


    private fun buildViewModel(): ItemEditViewModel {
        return ItemEditViewModel(
            cryptoManager = cryptoManager,
            itemRepository = itemRepo,
            clipboardManager = clipManager,
        )
    }

}

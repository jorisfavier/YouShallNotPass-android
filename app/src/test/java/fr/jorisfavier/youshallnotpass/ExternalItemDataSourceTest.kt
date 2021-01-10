package fr.jorisfavier.youshallnotpass

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.FileProvider
import fr.jorisfavier.youshallnotpass.data.impl.ExternalItemDataSource
import fr.jorisfavier.youshallnotpass.data.model.ItemDto
import fr.jorisfavier.youshallnotpass.manager.IContentResolverManager
import fr.jorisfavier.youshallnotpass.model.exception.YsnpException
import io.mockk.*
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import java.io.FileWriter

class ExternalItemDataSourceTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val appContext: Context = mockk()
    private val contentResolver: IContentResolverManager = mockk()
    private val dataSource = ExternalItemDataSource(appContext, contentResolver)
    private val fakeItemDto = ItemDto(title = "fakeTitle", login = "fakeLogin", password = "fakePassword")
    private val fakeItemDtoWithUrl = ItemDto(title = "test", login = "fakeLogin", password = "fakePassword")

    private val onePasswordExport = listOf(
        "\"Notes\",\"Password\",\"Title\",\"Type\",\"URL\",\"Username\",\n",
        ",\"fakePassword\",\"fakeTitle\",\"Login\",\"https://test.test\",\"fakeLogin\",",
        ",\"fakePassword\",\"fakeTitle\",\"Login\",\"https://test.test\",,",
    )
    private val dashLaneExport = listOf(
        "\"test.test\",\"test.test\",\"\",\"fakeLogin\",\"\",\"fakePassword\",\"\"",
        "\"test.test\",\"test.test\",\"\",,\"\",\"fakePassword\",\"\"",
    )
    private val lastPassExport = listOf(
        "url,username,password,totp,extra,name,grouping,fav\n",
        "http://,fakeLogin,fakePassword,,,fakeTitle,,0",
        "http://,,fakePassword,,,fakeTitle,,0"
    )
    private val chromeExport = listOf(
        "name,url,username,password\n",
        "fakeTitle,https://test.test,fakeLogin,fakePassword",
        "fakeTitle,https://test.test,,fakePassword"
    )
    private val bitwardenExport = listOf(
        "folder,favorite,type,name,notes,fields,login_uri,login_username,login_password,login_totp\n",
        ",,login,fakeTitle,,,,fakeLogin,fakePassword,",
        ",,login,fakeTitle,,,,,fakePassword,"
    )
    private val exportFirefox = listOf(
        "\"url\",\"username\",\"password\",\"httpRealm\",\"formActionOrigin\",\"guid\",\"timeCreated\",\"timeLastUsed\",\"timePasswordChanged\"\n",
        "\"https://test.test\",\"fakeLogin\",\"fakePassword\",,\"https://test.test\",\"{44596609-8731-f587-9141-nabba2343}\",\"13719493108328\",\"1534133619628\",\"1378893108328\"",
        "\"https://test.test\",\"\",\"fakePassword\",,\"https://test.test\",\"{44596609-8731-f587-9141-nabba2343}\",\"13719493108328\",\"1534133619628\",\"1378893108328\"",
        "\"https://www.test.test\",\"fakeLogin\",\"fakePassword\",,\"https://test.test\",\"{44596609-8731-f587-9141-nabba2343}\",\"13719493108328\",\"1534133619628\",\"1378893108328\"",
        "\"http://test.test\",\"fakeLogin\",\"fakePassword\",,\"https://test.test\",\"{44596609-8731-f587-9141-nabba2343}\",\"13719493108328\",\"1534133619628\",\"1378893108328\"",
        "\"http://www.test.test\",\"fakeLogin\",\"fakePassword\",,\"https://test.test\",\"{44596609-8731-f587-9141-nabba2343}\",\"13719493108328\",\"1534133619628\",\"1378893108328\"",
    )


    @Test
    fun `saveToCsv with a given list of item should save them as a csv file`() = runBlocking {
        //given
        mockkStatic(FileProvider::class)
        mockkConstructor(FileWriter::class)
        val slot = slot<String>()
        every { appContext.getExternalFilesDir(any()) } returns null
        every { FileProvider.getUriForFile(any(), any(), any()) } returns mockk()
        every { anyConstructed<FileWriter>().write(capture(slot)) } just runs
        every { anyConstructed<FileWriter>().flush() } just runs
        every { anyConstructed<FileWriter>().close() } just runs


        //when
        dataSource.saveToCsv(listOf(fakeItemDto))

        //then
        val fileContent = slot.captured
        val lines = fileContent.split("\n")
        TestCase.assertTrue(fileContent.isNotEmpty())
        TestCase.assertEquals(3, lines.size)
        TestCase.assertEquals("title,username,password", lines[0])
        TestCase.assertEquals("${fakeItemDto.title},${fakeItemDto.login},${fakeItemDto.password}", lines[1])
    }

    @Test
    fun `getItemsFromTextFile should extract items from onepassword export`() = runBlocking {
        //given
        coEvery { contentResolver.getFileContent(any()) } returns onePasswordExport

        //when
        val items = dataSource.getItemsFromTextFile(mockk())

        //then
        TestCase.assertEquals(2, items.size)
        TestCase.assertEquals(fakeItemDto, items.first())
        TestCase.assertTrue(items[1].login.isNullOrEmpty())
    }

    @Test
    fun `getItemsFromTextFile should extract items from lastpass export`() = runBlocking {
        //given
        coEvery { contentResolver.getFileContent(any()) } returns lastPassExport

        //when
        val items = dataSource.getItemsFromTextFile(mockk())

        //then
        TestCase.assertEquals(2, items.size)
        TestCase.assertEquals(fakeItemDto, items.first())
        TestCase.assertTrue(items[1].login.isNullOrEmpty())
    }

    @Test
    fun `getItemsFromTextFile should extract items from chrome export`() = runBlocking {
        //given
        coEvery { contentResolver.getFileContent(any()) } returns chromeExport

        //when
        val items = dataSource.getItemsFromTextFile(mockk())

        //then
        TestCase.assertEquals(2, items.size)
        TestCase.assertEquals(fakeItemDto, items.first())
        TestCase.assertTrue(items[1].login.isNullOrEmpty())
    }

    @Test
    fun `getItemsFromTextFile should extract items from bitwarden export`() = runBlocking {
        //given
        coEvery { contentResolver.getFileContent(any()) } returns bitwardenExport

        //when
        val items = dataSource.getItemsFromTextFile(mockk())

        //then
        TestCase.assertEquals(2, items.size)
        TestCase.assertEquals(fakeItemDto, items.first())
        TestCase.assertTrue(items[1].login.isNullOrEmpty())
    }

    @Test
    fun `getItemsFromTextFile should extract items from firefox export`() = runBlocking {
        //given
        coEvery { contentResolver.getFileContent(any()) } returns exportFirefox

        //when
        val items = dataSource.getItemsFromTextFile(mockk())

        //then
        TestCase.assertEquals(5, items.size)
        TestCase.assertEquals(fakeItemDtoWithUrl, items.first())
        TestCase.assertEquals(fakeItemDtoWithUrl, items[2])
        TestCase.assertEquals(fakeItemDtoWithUrl, items[3])
        TestCase.assertEquals(fakeItemDtoWithUrl, items[4])
        TestCase.assertTrue(items[1].login.isNullOrEmpty())
    }

    @Test(expected = YsnpException::class)
    fun `getItemsFromTextFile should emit error when importing without header`() = runBlocking {
        //given
        coEvery { contentResolver.getFileContent(any()) } returns dashLaneExport

        //when
        dataSource.getItemsFromTextFile(mockk())

        //then
        Assert.fail("An exception should be thrown")
    }
}
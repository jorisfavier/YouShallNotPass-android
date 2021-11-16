package fr.jorisfavier.youshallnotpass

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.jorisfavier.youshallnotpass.repository.DesktopRepository
import fr.jorisfavier.youshallnotpass.ui.desktop.DesktopConnectionViewModel
import fr.jorisfavier.youshallnotpass.utils.State
import io.mockk.*
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test

class DesktopConnectionViewModelTest {
    private val desktopRepository: DesktopRepository = mockk()

    private val viewModel by lazy { DesktopConnectionViewModel(desktopRepository) }

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `onCodeFound with a correct formatted code should emit a success state`() {
        //given
        val urlSlot = slot<String>()
        val publicKeySlot = slot<String>()
        val stateList = mutableListOf<State<Unit>>()
        val fakeUrl = "192.168.0.1:8080"
        val cleanedKey = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAtFidODAnunzeGxbO8Kt5"
        val fakeKey = "-----BEGIN PUBLIC KEY-----\n" +
                cleanedKey +
                "\n-----END PUBLIC KEY-----"
        val fakeCode = "$fakeUrl#ysnp#$fakeKey"
        coEvery {
            desktopRepository.updateDesktopInfo(capture(urlSlot), capture(publicKeySlot))
        } just runs

        //when
        viewModel.qrCodeAnalyseState.observeForever {
            stateList.add(it)
        }
        viewModel.onCodeFound(fakeCode)

        //then
        TestCase.assertEquals(2, stateList.size)
        TestCase.assertEquals(State.Loading, stateList.firstOrNull())
        TestCase.assertTrue(stateList[1] is State.Success)
        TestCase.assertEquals("http://$fakeUrl", urlSlot.captured)
        TestCase.assertEquals(cleanedKey, publicKeySlot.captured)
    }

    @Test
    fun `onCodeFound with an incorrect formatted code should emit a failure state`() {
        //given
        val stateList = mutableListOf<State<Unit>>()
        val fakeCode = "fakecode"
        coEvery {
            desktopRepository.updateDesktopInfo(any(), any())
        } just runs

        //when
        viewModel.qrCodeAnalyseState.observeForever {
            stateList.add(it)
        }
        viewModel.onCodeFound(fakeCode)

        //then
        TestCase.assertEquals(2, stateList.size)
        TestCase.assertEquals(State.Loading, stateList.firstOrNull())
        TestCase.assertEquals(State.Error, stateList[1])
        coVerify(inverse = true) { desktopRepository.updateDesktopInfo(any(), any()) }
    }

    @Test
    fun `onCodeFound with the repository throwing exception should emit a failure state`() {
        //given
        val stateList = mutableListOf<State<Unit>>()
        val fakeUrl = "192.168.0.1:8080"
        val cleanedKey = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAtFidODAnunzeGxbO8Kt5"
        val fakeKey = "-----BEGIN PUBLIC KEY-----\n" +
                cleanedKey +
                "\n-----END PUBLIC KEY-----"
        val fakeCode = "$fakeUrl#ysnp#$fakeKey"
        coEvery {
            desktopRepository.updateDesktopInfo(any(), any())
        } throws Exception()

        //when
        viewModel.qrCodeAnalyseState.observeForever {
            stateList.add(it)
        }
        viewModel.onCodeFound(fakeCode)

        //then
        TestCase.assertEquals(2, stateList.size)
        TestCase.assertEquals(State.Loading, stateList.firstOrNull())
        TestCase.assertEquals(State.Error, stateList[1])
    }

    @Test
    fun `onCodeFound with a correct formatted code should emit a success state only once`() {
        //given
        val urlSlot = slot<String>()
        val publicKeySlot = slot<String>()
        val stateList = mutableListOf<State<Unit>>()
        val fakeUrl = "192.168.0.1:8080"
        val cleanedKey = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAtFidODAnunzeGxbO8Kt5"
        val fakeKey = "-----BEGIN PUBLIC KEY-----\n" +
                cleanedKey +
                "\n-----END PUBLIC KEY-----"
        val fakeCode = "$fakeUrl#ysnp#$fakeKey"
        coEvery {
            desktopRepository.updateDesktopInfo(capture(urlSlot), capture(publicKeySlot))
        } just runs

        //when
        viewModel.qrCodeAnalyseState.observeForever {
            stateList.add(it)
        }
        viewModel.onCodeFound(fakeCode)
        viewModel.onCodeFound(fakeCode)
        viewModel.onCodeFound(fakeCode)
        viewModel.onCodeFound(fakeCode)

        //then
        TestCase.assertEquals(2, stateList.size)
        TestCase.assertEquals(State.Loading, stateList.firstOrNull())
        TestCase.assertTrue(stateList[1] is State.Success)
        TestCase.assertEquals("http://$fakeUrl", urlSlot.captured)
        TestCase.assertEquals(cleanedKey, publicKeySlot.captured)
    }

}
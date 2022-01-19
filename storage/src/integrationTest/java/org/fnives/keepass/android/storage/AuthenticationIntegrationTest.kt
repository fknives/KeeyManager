package org.fnives.keepass.android.storage

import java.io.File
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.internal.ActualKeePassRepository
import org.fnives.keepass.android.storage.model.Credentials
import org.fnives.keepass.android.storage.testutil.TestDispatcherHolder
import org.fnives.keepass.android.storage.testutil.copyResource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.exceptions.verification.NoInteractionsWanted
import org.mockito.kotlin.spy
import org.mockito.kotlin.verifyZeroInteractions

class AuthenticationIntegrationTest {

    private lateinit var sut: KeePassRepository
    private lateinit var testDispatcherHolder: TestDispatcherHolder
    private lateinit var databaseFile: File

    @BeforeEach
    fun setUp() {
        testDispatcherHolder = TestDispatcherHolder(startPaused = true)
        sut = ActualKeePassRepository.getInstance(
            dispatcherHolder = testDispatcherHolder.dispatcherHolder
        )
    }

    @AfterEach
    fun tearDown() {
        databaseFile.delete()
    }

    @DisplayName("GIVEN correct credentials WHEN accessed THEN is authenticated")
    @Test
    fun authenticationWithCorrectCredentials() = runBlocking {
        databaseFile = copyResource("entry-in-group-recyclebin-off.kdbx")
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
    }

    @DisplayName("GIVEN paused dispatcher WHEN accessed THEN times out")
    @Test
    fun onlySingleDispatcherIsUsedForAccess() {
        databaseFile = copyResource("entry-in-group-recyclebin-off.kdbx")

        val inputStream = spy(databaseFile.inputStream())

        val testCoroutineDispatcher = TestCoroutineDispatcher()
        val testScope = TestCoroutineScope(testCoroutineDispatcher)
        testScope.launch {
            sut.authenticate(Credentials(inputStream, { databaseFile.outputStream() }, "test1"))
        }

        testCoroutineDispatcher.advanceUntilIdle()
        verifyZeroInteractions(inputStream)
        testDispatcherHolder.single.runCurrent()
        Assertions.assertThrows(NoInteractionsWanted::class.java) {
            verifyZeroInteractions(inputStream)
        }
    }

    @DisplayName("GIVEN in correct credentials WHEN accessed THEN is authenticated")
    @Test
    fun authenticationWithInCorrectCredentials() {
        databaseFile = copyResource("entry-in-group-recyclebin-off.kdbx")
        testDispatcherHolder.single.resumeDispatcher()
        val exception = Assertions.assertThrows(AuthenticationException::class.java) {
            runBlocking { sut.authenticate(Credentials(databaseFile, "test2")) }
        }
        Assertions.assertEquals("Couldn't open database", exception.message)
        Assertions.assertFalse(exception.cause == null)
    }

    @Test
    fun disconnectTest() {
        TODO()
    }
}

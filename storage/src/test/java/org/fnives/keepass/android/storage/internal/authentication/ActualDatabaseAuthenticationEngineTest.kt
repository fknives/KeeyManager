package org.fnives.keepass.android.storage.internal.authentication

import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.internal.database.ActualDatabaseHolder
import org.fnives.keepass.android.storage.model.Credentials
import org.fnives.keepass.android.storage.testutil.TestDispatcherHolder
import org.fnives.keepass.android.storage.testutil.resourceAsStream
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions

@OptIn(ExperimentalCoroutinesApi::class)
internal class ActualDatabaseAuthenticationEngineTest {

    private lateinit var sut: ActualDatabaseAuthenticationEngine
    private lateinit var testDispatcherHolder: TestDispatcherHolder
    private lateinit var mockDatabaseHolder: ActualDatabaseHolder

    @BeforeEach
    fun setUp() {
        mockDatabaseHolder = mock()
        testDispatcherHolder = TestDispatcherHolder(startPaused = true)
        sut = ActualDatabaseAuthenticationEngine(
            dispatcherHolder = testDispatcherHolder.dispatcherHolder,
            databaseHolder = mockDatabaseHolder
        )
    }

    @DisplayName("WHEN_initialized_THEN_no_interactions")
    @Test
    fun initialized() {
        verifyZeroInteractions(mockDatabaseHolder)
    }

    @DisplayName("GIVEN_proper_database_WHEN_authenticating_THEN_its_opened")
    @Test
    fun successfulDataBaseOpening() = runBlocking {
        val inputStream: InputStream = resourceAsStream("test1.kdbx")
        val password = "test1"

        testDispatcherHolder.single.resumeDispatcher()
        val credentials = Credentials(
            databaseInputStream = inputStream,
            password = password,
            databaseOutputStreamFactory = { ByteArrayOutputStream() }
        )
        sut.authenticate(credentials)

        verify(mockDatabaseHolder, times(1))._database = any()
        verifyNoMoreInteractions(mockDatabaseHolder)
    }

    @DisplayName("GIVEN_open_database_WHEN_disconnecting_THEN_its_closed")
    @Test
    fun databaseClosing() = runBlocking {
        val inputStream: InputStream = resourceAsStream("test1.kdbx")
        val password = "test1"
        testDispatcherHolder.single.resumeDispatcher()
        val credentials = Credentials(
            databaseInputStream = inputStream,
            password = password,
            databaseOutputStreamFactory = { ByteArrayOutputStream() }
        )
        sut.authenticate(credentials)
        verify(mockDatabaseHolder, times(1))._database = any()

        sut.disconnect()

        verify(mockDatabaseHolder, times(1))._database
        verify(mockDatabaseHolder, times(1))._database = null
        verifyNoMoreInteractions(mockDatabaseHolder)
    }

    @DisplayName("GIVEN_invalid_password_WHEN_authenticating_THEN_exception_is_thrown")
    @Test
    fun invalidPassword() {
        val inputStream: InputStream = resourceAsStream("test1.kdbx")
        val password = "test2"

        Assertions.assertThrows(AuthenticationException::class.java) {
            runBlocking {
                testDispatcherHolder.single.resumeDispatcher()
                val credentials = Credentials(
                    databaseInputStream = inputStream,
                    password = password,
                    databaseOutputStreamFactory = { ByteArrayOutputStream() }
                )
                sut.authenticate(credentials)
            }
        }
    }

    @DisplayName("Verify Correct Dispatcher Is Used For Opening Database")
    @Test
    fun verifyCorrectDispatcherIsUsedForOpening() = runBlocking {
        val inputStream: InputStream = resourceAsStream("test1.kdbx")
        val password = "test1"
        val dispatcher = TestCoroutineDispatcher()

        async(dispatcher) {
            val credentials = Credentials(
                databaseInputStream = inputStream,
                password = password,
                databaseOutputStreamFactory = { ByteArrayOutputStream() }
            )
            sut.authenticate(credentials)
        }
        dispatcher.advanceUntilIdle()

        verifyZeroInteractions(mockDatabaseHolder)
        testDispatcherHolder.single.runCurrent()
        verify(mockDatabaseHolder, times(1))._database = any()
        verifyNoMoreInteractions(mockDatabaseHolder)
    }

    @DisplayName("Verify Correct Dispatcher Is Used For Closing Database")
    @Test
    fun verifyCorrectDispatcherIsUsed() = runBlocking {
        val dispatcher = TestCoroutineDispatcher()

        async(dispatcher) {
            sut.disconnect()
        }
        dispatcher.advanceUntilIdle()

        verifyZeroInteractions(mockDatabaseHolder)
        testDispatcherHolder.single.runCurrent()
        verify(mockDatabaseHolder, times(1))._database
        verify(mockDatabaseHolder, times(1))._database = null
        verifyNoMoreInteractions(mockDatabaseHolder)
    }
}
package org.fnives.keepass.android.storage.internal.entry

import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.internal.database.DatabaseHolder
import org.fnives.keepass.android.storage.internal.database.SavingDataBase
import org.fnives.keepass.android.storage.model.EntryId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class ActualEntryRepositoryDeleteTest {

    private lateinit var sut: EntryRepository
    private lateinit var mockDatabaseHolder: DatabaseHolder
    private lateinit var mockDatabase: SavingDataBase

    @BeforeEach
    fun setUp() {
        mockDatabase = mock()
        mockDatabaseHolder = mock()
        whenever(mockDatabaseHolder.database).thenReturn(mockDatabase)
        sut = ActualEntryRepository(mockDatabaseHolder)
    }

    @DisplayName("GIVEN proper EntryID WHEN deleting THEN its deleted from the DataBase")
    @Test
    fun deleteCallsTheProperMethods() = runBlocking {
        val entryId = EntryId(UUID(10, 20))

        sut.deleteEntry(entryId)

        val inOrder = inOrder(mockDatabase)
        inOrder.verify(mockDatabase).deleteEntry(entryId.uuid)
        inOrder.verify(mockDatabase).save()
        verifyNoMoreInteractions(mockDatabase)
    }

    @DisplayName("GIVEN throwing database THEN exception is thrown")
    @Test
    fun authenticationException() {
        whenever(mockDatabaseHolder.database).thenThrow(AuthenticationException("message"))
        val exception = Assertions.assertThrows(AuthenticationException::class.java) {
            runBlocking { sut.deleteEntry(EntryId(UUID(0, 0))) }
        }
        Assertions.assertEquals("message", exception.message)
        Assertions.assertEquals(null, exception.cause)
    }
}
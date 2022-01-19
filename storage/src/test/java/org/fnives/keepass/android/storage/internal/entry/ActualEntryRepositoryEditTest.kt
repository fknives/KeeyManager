package org.fnives.keepass.android.storage.internal.entry

import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.internal.database.DatabaseHolder
import org.fnives.keepass.android.storage.internal.database.SavingDataBase
import org.fnives.keepass.android.storage.model.EntryDetailed
import org.fnives.keepass.android.storage.model.EntryId
import org.fnives.keepass.android.storage.model.Group
import org.fnives.keepass.android.storage.model.GroupId
import org.fnives.keepass.android.storage.model.KIcon
import org.fnives.keepass.android.storage.testutil.createMockIcon
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.linguafranca.pwdb.kdbx.dom.DomEntryWrapper
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class ActualEntryRepositoryEditTest {

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

    @DisplayName("GIVEN proper entry WHEN added THEN its added to the database")
    @Test
    fun addEntrySuccess() = runBlocking {
        val mockIcon = createMockIcon(KIcon.Archive.ordinal)
        whenever(mockDatabase.newIcon(KIcon.Archive.ordinal)).doReturn(mockIcon)

        val mockDomToEdit = mock<DomEntryWrapper>()
        val entryId = EntryId(UUID(3, 4))
        whenever(mockDatabase.findEntry(entryId.uuid)).doReturn(mockDomToEdit)

        val entryDetailed = EntryDetailed(
            id = entryId,
            entryName = "1",
            userName = "2",
            password = "3",
            url = "4",
            notes = "5",
            icon = KIcon.Archive
        )

        sut.editEntry(entryDetailed)

        val inOrder = inOrder(mockDatabase)
        inOrder.verify(mockDatabase).findEntry(entryId.uuid)
        inOrder.verify(mockDatabase).newIcon(KIcon.Archive.ordinal)
        inOrder.verify(mockDatabase).save()
        verifyNoMoreInteractions(mockDatabase)

        verify(mockDomToEdit).title = "1"
        verify(mockDomToEdit).username = "2"
        verify(mockDomToEdit).password = "3"
        verify(mockDomToEdit).url = "4"
        verify(mockDomToEdit).notes = "5"
        verify(mockDomToEdit).icon = mockIcon
        verifyNoMoreInteractions(mockDomToEdit)
    }

    @DisplayName("GIVEN nonexistent entry WHEN edited THEN exception is thrown")
    @Test
    fun editNonExistentEntryThrows() = runBlocking {
        val entryId = EntryId(UUID(10000, 10000))
        whenever(mockDatabase.findEntry(entryId.uuid)).doReturn(null)
        val entryDetailed = EntryDetailed(
            id = entryId,
            entryName = "1",
            userName = "2",
            password = "3",
            url = "4",
            notes = "5",
            icon = KIcon.Archive
        )

        val expected = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlocking { sut.editEntry(entryDetailed) }
        }

        Assertions.assertEquals("Cannot edit non existent Entry", expected.message)
        Assertions.assertEquals(null, expected.cause)
        verify(mockDatabase).findEntry(entryId.uuid)
        verifyNoMoreInteractions(mockDatabase)
    }

    @DisplayName("GIVEN unauthenticated database holder WHEN edited THEN exception is thrown")
    @Test
    fun unauthenticated() {
        whenever(mockDatabaseHolder.database).doThrow(AuthenticationException("Database is not initialized / authenticated"))

        val entryDetailed = EntryDetailed(
            id = EntryId(UUID(10000, 10000)),
            entryName = "1",
            userName = "2",
            password = "3",
            url = "4",
            notes = "5",
            icon = KIcon.Archive
        )

        val expected = Assertions.assertThrows(AuthenticationException::class.java) {
            runBlocking { sut.editEntry(entryDetailed) }
        }

        Assertions.assertEquals("Database is not initialized / authenticated", expected.message)
        Assertions.assertEquals(null, expected.cause)
    }
}
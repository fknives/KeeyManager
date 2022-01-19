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
import org.linguafranca.pwdb.kdbx.dom.DomGroupWrapper
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class ActualEntryRepositoryAddTest {

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

    @DisplayName("GIVEN proper entry WHEN adding THEN its added to database")
    @Test
    fun addEntrySuccess() = runBlocking {
        val parentGroupId = GroupId(UUID(1, 2)) // notice
        val mockParentGroup = mock<DomGroupWrapper>()
        whenever(mockDatabase.findGroup(parentGroupId.uuid)).doReturn(mockParentGroup) // notice

        val mockIcon = createMockIcon(KIcon.Archive.ordinal)
        whenever(mockDatabase.newIcon(KIcon.Archive.ordinal)).doReturn(mockIcon)

        val mockNewDom = mock<DomEntryWrapper>()
        val expectedId = EntryId(UUID(3, 4))
        whenever(mockNewDom.uuid).doReturn(expectedId.uuid)
        whenever(mockDatabase.newEntry()).doReturn(mockNewDom)

        val entryDetailed = EntryDetailed(
            entryName = "1",
            userName = "2",
            password = "3",
            url = "4",
            notes = "5",
            icon = KIcon.Archive
        )

        val actualEntryId = sut.addEntry(entryDetailed, parentGroupId)

        val inOrder = inOrder(mockDatabase, mockParentGroup)
        inOrder.verify(mockDatabase).findGroup(parentGroupId.uuid) // notice
        inOrder.verify(mockDatabase).newEntry()
        inOrder.verify(mockDatabase).newIcon(KIcon.Archive.ordinal)
        inOrder.verify(mockParentGroup).addEntry(mockNewDom)
        inOrder.verify(mockDatabase).save()
        verifyNoMoreInteractions(mockDatabase)
        verifyNoMoreInteractions(mockParentGroup)

        verify(mockNewDom).title = "1"
        verify(mockNewDom).username = "2"
        verify(mockNewDom).password = "3"
        verify(mockNewDom).url = "4"
        verify(mockNewDom).notes = "5"
        verify(mockNewDom).icon = mockIcon
        verify(mockNewDom).uuid
        verifyNoMoreInteractions(mockNewDom)

        Assertions.assertEquals(expectedId, actualEntryId)
    }

    @DisplayName("GIVEN proper entry to ROOT WHEN adding THEN its added to database")
    @Test
    fun addEntrySuccessToRoot() = runBlocking {
        val parentGroupId = GroupId.ROOT_ID // notice
        val mockParentGroup = mock<DomGroupWrapper>()
        whenever(mockDatabase.rootGroup).doReturn(mockParentGroup) // notice

        val mockIcon = createMockIcon(KIcon.Archive.ordinal)
        whenever(mockDatabase.newIcon(KIcon.Archive.ordinal)).doReturn(mockIcon)

        val mockNewDom = mock<DomEntryWrapper>()
        val expectedId = EntryId(UUID(3, 4))
        whenever(mockNewDom.uuid).doReturn(expectedId.uuid)
        whenever(mockDatabase.newEntry()).doReturn(mockNewDom)

        val entryDetailed = EntryDetailed(
            entryName = "1",
            userName = "2",
            password = "3",
            url = "4",
            notes = "5",
            icon = KIcon.Archive
        )

        val actualEntryId = sut.addEntry(entryDetailed, parentGroupId)

        val inOrder = inOrder(mockDatabase, mockParentGroup)
        inOrder.verify(mockDatabase).rootGroup // notice
        inOrder.verify(mockDatabase).newEntry()
        inOrder.verify(mockDatabase).newIcon(KIcon.Archive.ordinal)
        inOrder.verify(mockParentGroup).addEntry(mockNewDom)
        inOrder.verify(mockDatabase).save()
        verifyNoMoreInteractions(mockDatabase)
        verifyNoMoreInteractions(mockParentGroup)

        verify(mockNewDom).title = "1"
        verify(mockNewDom).username = "2"
        verify(mockNewDom).password = "3"
        verify(mockNewDom).url = "4"
        verify(mockNewDom).notes = "5"
        verify(mockNewDom).icon = mockIcon
        verify(mockNewDom).uuid
        verifyNoMoreInteractions(mockNewDom)

        Assertions.assertEquals(expectedId, actualEntryId)
    }

    @DisplayName("GIVEN proper entry to Non Existent parent WHEN adding THEN exception is thrown")
    @Test
    fun addEntryToNonExistentParent() = runBlocking {
        val parentGroupId = GroupId(UUID(5, 6)) // notice
        whenever(mockDatabase.findGroup(parentGroupId.uuid)).doReturn(null) // notice
        val entryDetailed = EntryDetailed(
            entryName = "1",
            userName = "2",
            password = "3",
            url = "4",
            notes = "5",
            icon = KIcon.Archive
        )

        val expected = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlocking { sut.addEntry(entryDetailed, parentGroupId) }
        }
        Assertions.assertEquals("Couldn't find parent to add entry to", expected.message)
        Assertions.assertEquals(null, expected.cause)

        verify(mockDatabase).findGroup(parentGroupId.uuid) // notice
        verifyNoMoreInteractions(mockDatabase)
    }

    @DisplayName("GIVEN proper entry to Non Existent ROOT WHEN adding THEN exception is thrown")
    @Test
    fun addEntryToNonExistentROOT() = runBlocking {
        val parentGroupId = GroupId.ROOT_ID // notice
        whenever(mockDatabase.rootGroup).doReturn(null) // notice
        val entryDetailed = EntryDetailed(
            entryName = "1",
            userName = "2",
            password = "3",
            url = "4",
            notes = "5",
            icon = KIcon.Archive
        )

        val expected = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlocking { sut.addEntry(entryDetailed, parentGroupId) }
        }
        Assertions.assertEquals("Couldn't find parent to add entry to", expected.message)
        Assertions.assertEquals(null, expected.cause)

        verify(mockDatabase).rootGroup // notice
        verifyNoMoreInteractions(mockDatabase)
    }

    @DisplayName("GIVEN unauthenticated database holder WHEN added THEN exception is thrown")
    @Test
    fun unauthenticated() {
        whenever(mockDatabaseHolder.database).doThrow(AuthenticationException("Database is not initialized / authenticated"))
        val entryDetailed = EntryDetailed(
            entryName = "1",
            userName = "2",
            password = "3",
            url = "4",
            notes = "5",
            icon = KIcon.Archive
        )

        val expected = Assertions.assertThrows(AuthenticationException::class.java) {
            runBlocking { sut.addEntry(entryDetailed, GroupId.ROOT_ID) }
        }

        Assertions.assertEquals("Database is not initialized / authenticated", expected.message)
        Assertions.assertEquals(null, expected.cause)
    }
}
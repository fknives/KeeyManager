package org.fnives.keepass.android.storage.internal.entry

import java.util.Date
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.internal.database.DatabaseHolder
import org.fnives.keepass.android.storage.internal.database.SavingDataBase
import org.fnives.keepass.android.storage.model.EntryDetailed
import org.fnives.keepass.android.storage.model.EntryId
import org.fnives.keepass.android.storage.model.KIcon
import org.fnives.keepass.android.storage.testutil.createMockIcon
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.linguafranca.pwdb.kdbx.dom.DomEntryWrapper
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ActualEntryRepositoryGetTest {

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

    @DisplayName("GIVEN proper Entry WHEN requested THEN its parsed and returned")
    @Test
    fun verifyEntryParsing() = runBlocking {
        val entryId = EntryId(UUID(5,4))
        val expectedEntryDetailed = EntryDetailed(
            id = entryId,
            entryName = "1",
            userName = "2",
            password = "3",
            url = "4",
            notes = "5",
            icon = KIcon.Archive,
            lastModified = Date(50)
        )
        val mockEntryDom = mock<DomEntryWrapper>()
        whenever(mockEntryDom.uuid).doReturn(entryId.uuid)
        whenever(mockEntryDom.title).doReturn("1")
        whenever(mockEntryDom.username).doReturn("2")
        whenever(mockEntryDom.password).doReturn("3")
        whenever(mockEntryDom.url).doReturn("4")
        whenever(mockEntryDom.notes).doReturn("5")
        whenever(mockEntryDom.lastModificationTime).doReturn(Date(50))
        val mockIcon = createMockIcon(KIcon.Archive.ordinal)
        whenever(mockEntryDom.icon).doReturn(mockIcon)
        whenever(mockDatabase.findEntry(entryId.uuid)).doReturn(mockEntryDom)

        val actual = sut.getEntry(entryId)

        Assertions.assertEquals(expectedEntryDetailed, actual)
    }

    @DisplayName("GIVEN proper Entry with invalid icon WHEN requested THEN its parsed and returned")
    @Test
    fun verifyInvalidIConParsing() = runBlocking {
        val entryId = EntryId(UUID(5,4))
        val expectedEntryDetailed = EntryDetailed(
            id = entryId,
            entryName = "1",
            userName = "2",
            password = "3",
            url = "4",
            notes = "5",
            icon = KIcon.UNKNOWN,
            lastModified = Date(50)
        )
        val mockEntryDom = mock<DomEntryWrapper>()
        whenever(mockEntryDom.uuid).doReturn(entryId.uuid)
        whenever(mockEntryDom.title).doReturn("1")
        whenever(mockEntryDom.username).doReturn("2")
        whenever(mockEntryDom.password).doReturn("3")
        whenever(mockEntryDom.url).doReturn("4")
        whenever(mockEntryDom.notes).doReturn("5")
        whenever(mockEntryDom.lastModificationTime).doReturn(Date(50))
        val mockIcon = createMockIcon(Int.MAX_VALUE)
        whenever(mockEntryDom.icon).doReturn(mockIcon)
        whenever(mockDatabase.findEntry(entryId.uuid)).doReturn(mockEntryDom)

        val actual = sut.getEntry(entryId)

        Assertions.assertEquals(expectedEntryDetailed, actual)
    }

    @DisplayName("GIVEN notFoundEntry WHEN requested THEN its parsed and returned")
    @Test
    fun notFoundEntry() = runBlocking {
        val entryId = EntryId(UUID(5,4))
        whenever(mockDatabase.findEntry(entryId.uuid)).doReturn(null)

        val actual = sut.getEntry(entryId)

        Assertions.assertEquals(null, actual)
    }

    @DisplayName("GIVEN unauthenticated database holder WHEN edited THEN exception is thrown")
    @Test
    fun unauthenticated() {
        whenever(mockDatabaseHolder.database).doThrow(AuthenticationException("Database is not initialized / authenticated"))
        val entryId = EntryId(UUID(10000, 10000))

        val expected = Assertions.assertThrows(AuthenticationException::class.java) {
            runBlocking { sut.getEntry(entryId) }
        }

        Assertions.assertEquals("Database is not initialized / authenticated", expected.message)
        Assertions.assertEquals(null, expected.cause)
    }
}
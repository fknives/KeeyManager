package org.fnives.keepass.android.storage.internal.search

import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.fnives.keepass.android.storage.internal.database.DatabaseHolder
import org.fnives.keepass.android.storage.internal.database.SavingDataBase
import org.fnives.keepass.android.storage.model.Entry
import org.fnives.keepass.android.storage.model.EntryId
import org.fnives.keepass.android.storage.model.GroupId
import org.fnives.keepass.android.storage.model.GroupOrEntry
import org.fnives.keepass.android.storage.model.KIcon
import org.fnives.keepass.android.storage.testutil.createMockIcon
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.linguafranca.pwdb.Entry as DBEntry
import org.linguafranca.pwdb.kdbx.dom.DomEntryWrapper
import org.linguafranca.pwdb.kdbx.dom.DomGroupWrapper
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ActualSearchEngineByUsernameTest {

    private lateinit var sut: SearchEngine
    private lateinit var mockDatabaseHolder: DatabaseHolder
    private lateinit var mockDatabase: SavingDataBase

    @BeforeEach
    fun setUp() {
        mockDatabase = mock()
        mockDatabaseHolder = mock()
        whenever(mockDatabaseHolder.database).thenReturn(mockDatabase)
        sut = ActualSearchEngine(mockDatabaseHolder)
    }

    @Test
    fun searchInRootEmpty() = runBlocking {
        val mockRoot = mock<DomGroupWrapper>()
        whenever(mockRoot.findEntries(anyOrNull<DBEntry.Matcher>(), anyOrNull())).doReturn(emptyList())
        whenever(mockDatabase.rootGroup).doReturn(mockRoot)

        val actual = sut.searchByUsername(username = "sis", scope = GroupId.ROOT_ID)

        Assertions.assertEquals(emptyList<GroupOrEntry>(), actual)
    }

    @Test
    fun searchInRootMatching() = runBlocking {
        val mockEntryIcon = createMockIcon(KIcon.Clock.ordinal)
        val mockMatchingEntry = mock<DomEntryWrapper>()
        whenever(mockMatchingEntry.uuid).doReturn(UUID(1, 2))
        whenever(mockMatchingEntry.title).doReturn("sissyyy")
        whenever(mockMatchingEntry.username).doReturn("uff")
        whenever(mockMatchingEntry.icon).doReturn(mockEntryIcon)

        val mockEntryIcon2 = createMockIcon(Int.MIN_VALUE)
        val mockMatchingEntry2 = mock<DomEntryWrapper>()
        whenever(mockMatchingEntry2.uuid).doReturn(UUID(1, 3))
        whenever(mockMatchingEntry2.title).doReturn("alma")
        whenever(mockMatchingEntry2.username).doReturn("bananos")
        whenever(mockMatchingEntry2.icon).doReturn(mockEntryIcon2)

        val mockRoot = mock<DomGroupWrapper>()
        whenever(mockRoot.findEntries(anyOrNull<DBEntry.Matcher>(), anyOrNull())).doReturn(listOf(mockMatchingEntry, mockMatchingEntry2))
        whenever(mockDatabase.rootGroup).doReturn(mockRoot)

        val expected = listOf(
            Entry(id = EntryId(UUID(1, 2)), entryName = "sissyyy", userName = "uff", icon = KIcon.Clock),
            Entry(id = EntryId(UUID(1, 3)), entryName = "alma", userName = "bananos", icon = KIcon.UNKNOWN)
        )

        val actual = sut.searchByUsername(username = "sis", scope = GroupId.ROOT_ID)

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun verifyEntryMatcher() = runBlocking {
        val secondArgumentCaptor = argumentCaptor<Boolean>()
        val matcherArgumentCaptor = argumentCaptor<DBEntry.Matcher>()
        val mockRoot = mock<DomGroupWrapper>()
        whenever(mockRoot.findEntries(matcherArgumentCaptor.capture(), secondArgumentCaptor.capture())).doReturn(emptyList())
        whenever(mockDatabase.rootGroup).doReturn(mockRoot)
        sut.searchByUsername(username = "sis", scope = GroupId.ROOT_ID)

        Assertions.assertEquals(listOf(true), secondArgumentCaptor.allValues)
        Assertions.assertEquals(1, matcherArgumentCaptor.allValues.size)
        val actualArgumentCaptor = matcherArgumentCaptor.firstValue

        val mockDBEntry = mock<DBEntry<*, *, *, *>>()

        whenever(mockDBEntry.username).doReturn("sis")
        val actual = actualArgumentCaptor.matches(mockDBEntry)
        Assertions.assertEquals(true, actual)

        whenever(mockDBEntry.username).doReturn("sisyyyy")
        val actual2 = actualArgumentCaptor.matches(mockDBEntry)
        Assertions.assertEquals(true, actual2)

        whenever(mockDBEntry.username).doReturn("si")
        val actual3 = actualArgumentCaptor.matches(mockDBEntry)
        Assertions.assertEquals(false, actual3)

        whenever(mockDBEntry.username).doReturn("SiS")
        val actual4 = actualArgumentCaptor.matches(mockDBEntry)
        Assertions.assertEquals(true, actual4)
    }

    @Test
    fun searchInGroupMatching() = runBlocking {
        val mockEntryIcon = createMockIcon(KIcon.Clock.ordinal)
        val mockMatchingEntry = mock<DomEntryWrapper>()
        whenever(mockMatchingEntry.uuid).doReturn(UUID(1, 2))
        whenever(mockMatchingEntry.title).doReturn("sissyyy")
        whenever(mockMatchingEntry.username).doReturn("uff")
        whenever(mockMatchingEntry.icon).doReturn(mockEntryIcon)

        val mockEntryIcon2 = createMockIcon(Int.MIN_VALUE)
        val mockMatchingEntry2 = mock<DomEntryWrapper>()
        whenever(mockMatchingEntry2.uuid).doReturn(UUID(1, 3))
        whenever(mockMatchingEntry2.title).doReturn("alma")
        whenever(mockMatchingEntry2.username).doReturn("bananos")
        whenever(mockMatchingEntry2.icon).doReturn(mockEntryIcon2)

        val mockGroup = mock<DomGroupWrapper>()
        whenever(mockGroup.findEntries(anyOrNull<DBEntry.Matcher>(), anyOrNull())).doReturn(listOf(mockMatchingEntry, mockMatchingEntry2))
        whenever(mockDatabase.findGroup(UUID(123, 321))).doReturn(mockGroup)

        val expected = listOf(
            Entry(id = EntryId(UUID(1, 2)), entryName = "sissyyy", userName = "uff", icon = KIcon.Clock),
            Entry(id = EntryId(UUID(1, 3)), entryName = "alma", userName = "bananos", icon = KIcon.UNKNOWN)
        )

        val actual = sut.searchByUsername(username = "sis", scope = GroupId(UUID(123, 321)))

        Assertions.assertEquals(expected, actual)
    }
}

package org.fnives.keepass.android.storage.internal.search

import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.internal.database.DatabaseHolder
import org.fnives.keepass.android.storage.internal.database.SavingDataBase
import org.fnives.keepass.android.storage.model.Entry
import org.fnives.keepass.android.storage.model.EntryId
import org.fnives.keepass.android.storage.model.Group
import org.fnives.keepass.android.storage.model.GroupId
import org.fnives.keepass.android.storage.model.GroupOrEntry
import org.fnives.keepass.android.storage.model.KIcon
import org.fnives.keepass.android.storage.testutil.createMockIcon
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.linguafranca.pwdb.Entry as DBEntry
import org.linguafranca.pwdb.kdbx.dom.DomEntryWrapper
import org.linguafranca.pwdb.kdbx.dom.DomGroupWrapper
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ActualSearchEngineByNameTest {

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
        whenever(mockRoot.groups).doReturn(emptyList())
        whenever(mockRoot.findEntries(anyOrNull<DBEntry.Matcher>(), anyOrNull())).doReturn(emptyList())
        whenever(mockDatabase.rootGroup).doReturn(mockRoot)

        val actual = sut.search(name = "sis", scope = GroupId.ROOT_ID)

        Assertions.assertEquals(emptyList<GroupOrEntry>(), actual)
    }

    @Test
    fun searchInNonExistentRootGroup() = runBlocking {
        whenever(mockDatabase.rootGroup).doReturn(null)

        val actual = sut.search(name = "sis", scope = GroupId.ROOT_ID)

        Assertions.assertEquals(emptyList<GroupOrEntry>(), actual)
    }

    @Test
    fun searchInNonExistentGroup() = runBlocking {
        whenever(mockDatabase.findGroup(UUID(100, 200))).doReturn(null)

        val actual = sut.search(name = "sis", scope = GroupId(UUID(100, 200)))

        Assertions.assertEquals(emptyList<GroupOrEntry>(), actual)
    }

    @Test
    fun verifyEntryMatcher() = runBlocking {
        val mockRoot = mock<DomGroupWrapper>()
        whenever(mockRoot.groups).doReturn(emptyList())
        val secondArgumentCaptor = argumentCaptor<Boolean>()
        val matcherArgumentCaptor = argumentCaptor<DBEntry.Matcher>()
        whenever(mockRoot.findEntries(matcherArgumentCaptor.capture(), secondArgumentCaptor.capture())).doReturn(emptyList())
        whenever(mockDatabase.rootGroup).doReturn(mockRoot)
        sut.search(name = "sis", scope = GroupId.ROOT_ID)

        Assertions.assertEquals(listOf(true), secondArgumentCaptor.allValues)
        Assertions.assertEquals(1, matcherArgumentCaptor.allValues.size)
        val actualArgumentCaptor = matcherArgumentCaptor.firstValue

        val mockDBEntry = mock<DBEntry<*, *, *, *>>()

        whenever(mockDBEntry.title).doReturn("sis")
        val actual = actualArgumentCaptor.matches(mockDBEntry)
        Assertions.assertEquals(true, actual)

        whenever(mockDBEntry.title).doReturn("sisyyyy")
        val actual2 = actualArgumentCaptor.matches(mockDBEntry)
        Assertions.assertEquals(true, actual2)

        whenever(mockDBEntry.title).doReturn("si")
        val actual3 = actualArgumentCaptor.matches(mockDBEntry)
        Assertions.assertEquals(false, actual3)

        whenever(mockDBEntry.title).doReturn("SiS")
        val actual4 = actualArgumentCaptor.matches(mockDBEntry)
        Assertions.assertEquals(true, actual4)
    }

    @Test
    fun searchFlatInRootMatching() = runBlocking {
        val mockMatchingGroup = mock<DomGroupWrapper>()
        whenever(mockMatchingGroup.name).doReturn("sissy")
        val mockGroupIcon = createMockIcon(KIcon.Archive.ordinal)
        whenever(mockMatchingGroup.uuid).doReturn(UUID(1, 1))
        whenever(mockMatchingGroup.icon).doReturn(mockGroupIcon)
        whenever(mockMatchingGroup.entriesCount).doReturn(10)
        whenever(mockMatchingGroup.groupsCount).doReturn(5)
        whenever(mockMatchingGroup.groups).doReturn(emptyList())
        whenever(mockMatchingGroup.entries).doReturn(emptyList())

        val mockEntryIcon = createMockIcon(KIcon.Clock.ordinal)
        val mockMatchingEntry = mock<DomEntryWrapper>()
        whenever(mockMatchingEntry.uuid).doReturn(UUID(1, 2))
        whenever(mockMatchingEntry.title).doReturn("sissyyy")
        whenever(mockMatchingEntry.username).doReturn("uff")
        whenever(mockMatchingEntry.icon).doReturn(mockEntryIcon)

        val mockRoot = mock<DomGroupWrapper>()
        whenever(mockRoot.groups).doReturn(listOf(mockMatchingGroup))
        whenever(mockRoot.entries).doReturn(emptyList())
        whenever(mockRoot.findEntries(anyOrNull<DBEntry.Matcher>(), anyOrNull())).doReturn(listOf(mockMatchingEntry))
        whenever(mockDatabase.rootGroup).doReturn(mockRoot)

        val expected = listOf(
            Group(id = GroupId(UUID(1, 1)), groupName = "sissy", entryOrGroupCount = 15, icon = KIcon.Archive),
            Entry(id = EntryId(UUID(1, 2)), entryName = "sissyyy", userName = "uff", icon = KIcon.Clock)
        )

        val actual = sut.search(name = "sis", scope = GroupId.ROOT_ID)

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun searchDeepInRootMatching() = runBlocking {
        val mockMatchingEntry = mock<DomEntryWrapper>() // but not returned by findEntries from root
        whenever(mockMatchingEntry.uuid).doReturn(UUID(1, 2))
        whenever(mockMatchingEntry.title).doReturn("sissyyy")
        whenever(mockMatchingEntry.username).doReturn("uff")
        val mockEntryIcon = createMockIcon(KIcon.Clock.ordinal)
        whenever(mockMatchingEntry.icon).doReturn(mockEntryIcon)

        val mockMatchingEntry2 = mock<DomEntryWrapper>()
        whenever(mockMatchingEntry2.uuid).doReturn(UUID(2, 2))
        whenever(mockMatchingEntry2.title).doReturn("sisy")
        whenever(mockMatchingEntry2.username).doReturn("111")
        val mockEntryIcon2 = createMockIcon(Int.MAX_VALUE)
        whenever(mockMatchingEntry2.icon).doReturn(mockEntryIcon2)

        val mockMatchingGroup = mock<DomGroupWrapper>()
        whenever(mockMatchingGroup.name).doReturn("sissy")
        val mockGroupIcon = createMockIcon(KIcon.Archive.ordinal)
        whenever(mockMatchingGroup.uuid).doReturn(UUID(1, 1))
        whenever(mockMatchingGroup.icon).doReturn(mockGroupIcon)
        whenever(mockMatchingGroup.entriesCount).doReturn(10)
        whenever(mockMatchingGroup.groupsCount).doReturn(5)
        whenever(mockMatchingGroup.groups).doReturn(emptyList())
        whenever(mockMatchingGroup.findEntries(anyOrNull<DBEntry.Matcher>(), anyOrNull())).doReturn(listOf(mockMatchingEntry))

        val mockNonMatchingGroup = mock<DomGroupWrapper>()
        whenever(mockNonMatchingGroup.name).doReturn("si")
        whenever(mockNonMatchingGroup.groups).doReturn(listOf(mockMatchingGroup))
        whenever(mockNonMatchingGroup.findEntries(anyOrNull<DBEntry.Matcher>(), anyOrNull())).doReturn(listOf(mockMatchingEntry2))

        val mockRoot = mock<DomGroupWrapper>()
        whenever(mockRoot.groups).doReturn(listOf(mockNonMatchingGroup))
        whenever(mockRoot.findEntries(anyOrNull<DBEntry.Matcher>(), anyOrNull())).doReturn(listOf(mockMatchingEntry2))
        whenever(mockDatabase.rootGroup).doReturn(mockRoot)

        val expected = listOf(
            Group(id = GroupId(UUID(1, 1)), groupName = "sissy", entryOrGroupCount = 15, icon = KIcon.Archive),
            Entry(id = EntryId(UUID(2, 2)), entryName = "sisy", userName = "111", icon = KIcon.UNKNOWN)
        )

        val actual = sut.search(name = "sis", scope = GroupId.ROOT_ID)

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun searchFlatInGroupMatching() = runBlocking {
        val mockMatchingGroup = mock<DomGroupWrapper>()
        whenever(mockMatchingGroup.name).doReturn("sissy")
        val mockGroupIcon = createMockIcon(KIcon.Archive.ordinal)
        whenever(mockMatchingGroup.uuid).doReturn(UUID(1, 1))
        whenever(mockMatchingGroup.icon).doReturn(mockGroupIcon)
        whenever(mockMatchingGroup.entriesCount).doReturn(10)
        whenever(mockMatchingGroup.groupsCount).doReturn(5)
        whenever(mockMatchingGroup.groups).doReturn(emptyList())
        whenever(mockMatchingGroup.entries).doReturn(emptyList())

        val mockEntryIcon = createMockIcon(KIcon.Clock.ordinal)
        val mockMatchingEntry = mock<DomEntryWrapper>()
        whenever(mockMatchingEntry.uuid).doReturn(UUID(1, 2))
        whenever(mockMatchingEntry.title).doReturn("sissyyy")
        whenever(mockMatchingEntry.username).doReturn("uff")
        whenever(mockMatchingEntry.icon).doReturn(mockEntryIcon)

        val mockGroup = mock<DomGroupWrapper>()
        whenever(mockGroup.groups).doReturn(listOf(mockMatchingGroup))
        whenever(mockGroup.entries).doReturn(emptyList())
        whenever(mockGroup.findEntries(anyOrNull<DBEntry.Matcher>(), anyOrNull())).doReturn(listOf(mockMatchingEntry))
        whenever(mockDatabase.findGroup(UUID(1000, 1000))).doReturn(mockGroup)

        val expected = listOf(
            Group(id = GroupId(UUID(1, 1)), groupName = "sissy", entryOrGroupCount = 15, icon = KIcon.Archive),
            Entry(id = EntryId(UUID(1, 2)), entryName = "sissyyy", userName = "uff", icon = KIcon.Clock)
        )

        val actual = sut.search(name = "sis", scope = GroupId(UUID(1000, 1000)))

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN throwing database THEN exception is thrown")
    @Test
    fun authenticationException() {
        whenever(mockDatabaseHolder.database).thenThrow(AuthenticationException())
        Assertions.assertThrows(AuthenticationException::class.java) {
            runBlocking { sut.search(name = "", scope = GroupId.ROOT_ID) }
        }
    }
}

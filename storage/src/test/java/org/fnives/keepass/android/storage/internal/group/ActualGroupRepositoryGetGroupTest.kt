package org.fnives.keepass.android.storage.internal.group

import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.internal.database.DatabaseHolder
import org.fnives.keepass.android.storage.internal.database.SavingDataBase
import org.fnives.keepass.android.storage.model.Entry
import org.fnives.keepass.android.storage.model.EntryId
import org.fnives.keepass.android.storage.model.Group
import org.fnives.keepass.android.storage.model.GroupId
import org.fnives.keepass.android.storage.model.GroupWithEntries
import org.fnives.keepass.android.storage.model.KIcon
import org.fnives.keepass.android.storage.testutil.createMockIcon
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.linguafranca.pwdb.kdbx.dom.DomEntryWrapper
import org.linguafranca.pwdb.kdbx.dom.DomGroupWrapper
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever

internal class ActualGroupRepositoryGetGroupTest {

    private lateinit var sut: GroupRepository
    private lateinit var mockDatabaseHolder: DatabaseHolder
    private lateinit var mockDatabase: SavingDataBase

    @BeforeEach
    fun setUp() {
        mockDatabase = mock()
        mockDatabaseHolder = mock()
        whenever(mockDatabaseHolder.database).thenReturn(mockDatabase)
        sut = ActualGroupRepository(mockDatabaseHolder)
    }

    @DisplayName("WHEN initialized THEN nothing is touched")
    @Test
    fun initialization() {
        verifyZeroInteractions(mockDatabaseHolder)
    }

    @DisplayName("GIVEN empty group WHEN queried THEN it's returned parsed")
    @Test
    fun verifyEmptyGroupParsing() = runBlocking {
        val queryId = UUID(0, 0)
        val returnedId = UUID(1, 1)
        val mockedDom = mock<DomGroupWrapper>()
        whenever(mockedDom.uuid).doReturn(returnedId)
        whenever(mockedDom.name).doReturn("title")
        whenever(mockedDom.entries).doReturn(emptyList())
        whenever(mockedDom.groups).doReturn(emptyList())
        whenever(mockedDom.groupsCount).doReturn(0)
        whenever(mockedDom.entriesCount).doReturn(0)
        val mockIcon = createMockIcon(KIcon.CDRom.ordinal)
        whenever(mockedDom.icon).doReturn(mockIcon)
        whenever(mockDatabase.findGroup(queryId)).thenReturn(mockedDom)
        val expected = GroupWithEntries(
            group = Group(id = GroupId(returnedId), groupName = "title", icon = KIcon.CDRom, entryOrGroupCount = 0),
            entries = emptyList()
        )

        val group = sut.getGroup(GroupId(queryId))

        Assertions.assertEquals(expected, group)
    }

    @DisplayName("GIVEN group with entries WHEN queried THEN it's returned parsed")
    @Test
    fun verifyGroupWithEntriesParsing() = runBlocking {
        val queryId = UUID(0, 0)
        val returnedId = UUID(1, 1)

        val returnedEntryId = UUID(2, 2)
        val mockedEntryDom = mock<DomEntryWrapper>()
        whenever(mockedEntryDom.uuid).doReturn(returnedEntryId)
        whenever(mockedEntryDom.title).doReturn("cekla")
        whenever(mockedEntryDom.username).doReturn("cekla-2")
        val mockIcon = createMockIcon(Int.MAX_VALUE)
        whenever(mockedEntryDom.icon).doReturn(mockIcon)

        val mockedDom = mock<DomGroupWrapper>()
        whenever(mockedDom.uuid).doReturn(returnedId)
        whenever(mockedDom.name).doReturn("title")
        whenever(mockedDom.entries).doReturn(listOf(mockedEntryDom))
        whenever(mockedDom.groups).doReturn(emptyList())
        whenever(mockedDom.entriesCount).doReturn(1)
        whenever(mockedDom.groupsCount).doReturn(0)
        val mockIcon2 = createMockIcon(Int.MIN_VALUE)
        whenever(mockedDom.icon).doReturn(mockIcon2)

        whenever(mockDatabase.findGroup(queryId)).thenReturn(mockedDom)

        val expected = GroupWithEntries(
            group = Group(id = GroupId(returnedId), groupName = "title", icon = KIcon.UNKNOWN, entryOrGroupCount = 1),
            entries = listOf(Entry(id = EntryId(returnedEntryId), entryName = "cekla", userName = "cekla-2", icon = KIcon.UNKNOWN))
        )

        val group = sut.getGroup(GroupId(queryId))

        Assertions.assertEquals(expected, group)
    }

    @DisplayName("GIVEN group with subgroups WHEN queried THEN it's returned parsed")
    @Test
    fun verifyGroupWithSubGroupsParsing() = runBlocking {
        val queryId = UUID(0, 0)
        val returnedId = UUID(1, 1)

        val returnedSubGroupId = UUID(2, 2)
        val mockedSubGroupDom = mock<DomGroupWrapper>()
        whenever(mockedSubGroupDom.uuid).doReturn(returnedSubGroupId)
        whenever(mockedSubGroupDom.name).doReturn("cekla")
        val mockIcon = createMockIcon(KIcon.Book.ordinal)
        whenever(mockedSubGroupDom.icon).doReturn(mockIcon)
        whenever(mockedSubGroupDom.entriesCount).doReturn(10)
        whenever(mockedSubGroupDom.groupsCount).doReturn(5)

        val mockedDom = mock<DomGroupWrapper>()
        whenever(mockedDom.uuid).doReturn(returnedId)
        whenever(mockedDom.name).doReturn("title")
        whenever(mockedDom.entries).doReturn(emptyList())
        whenever(mockedDom.groups).doReturn(listOf(mockedSubGroupDom))
        whenever(mockedDom.entriesCount).doReturn(0)
        whenever(mockedDom.groupsCount).doReturn(1)
        val mockIcon2 = createMockIcon(KIcon.Apple.ordinal)
        whenever(mockedDom.icon).doReturn(mockIcon2)

        whenever(mockDatabase.findGroup(queryId)).thenReturn(mockedDom)

        val expected = GroupWithEntries(
            group = Group(id = GroupId(returnedId), groupName = "title", icon = KIcon.Apple, entryOrGroupCount = 1),
            entries = listOf(Group(id = GroupId(returnedSubGroupId), groupName = "cekla", icon = KIcon.Book, entryOrGroupCount = 15))
        )

        val group = sut.getGroup(GroupId(queryId))

        Assertions.assertEquals(expected, group)
    }

    @DisplayName("GIVEN group with subgroups and entries WHEN queried THEN it's returned parsed")
    @Test
    fun verifyGroupWithSubGroupsAndEntriesParsing() = runBlocking {
        val queryId = UUID(0, 0)
        val returnedId = UUID(1, 1)

        val returnedSubGroupId = UUID(2, 2)
        val mockedSubGroupDom = mock<DomGroupWrapper>()
        whenever(mockedSubGroupDom.uuid).doReturn(returnedSubGroupId)
        whenever(mockedSubGroupDom.name).doReturn("cekla")
        val mockIcon = createMockIcon(KIcon.Certificate.ordinal)
        whenever(mockedSubGroupDom.icon).doReturn(mockIcon)

        val returnedEntryId = UUID(3, 3)
        val mockedEntryDom = mock<DomEntryWrapper>()
        whenever(mockedEntryDom.uuid).doReturn(returnedEntryId)
        whenever(mockedEntryDom.title).doReturn("cekla")
        val mockIcon2 = createMockIcon(KIcon.Clock.ordinal)
        whenever(mockedEntryDom.icon).doReturn(mockIcon2)

        val mockedDom = mock<DomGroupWrapper>()
        whenever(mockedDom.uuid).doReturn(returnedId)
        whenever(mockedDom.name).doReturn("title")
        whenever(mockedDom.entries).doReturn(listOf(mockedEntryDom))
        whenever(mockedDom.groups).doReturn(listOf(mockedSubGroupDom))
        whenever(mockedDom.entriesCount).doReturn(1)
        whenever(mockedDom.groupsCount).doReturn(1)
        val mockIcon3 = createMockIcon(KIcon.WorldStar.ordinal)
        whenever(mockedDom.icon).doReturn(mockIcon3)

        whenever(mockDatabase.findGroup(queryId)).thenReturn(mockedDom)

        val expected = GroupWithEntries(
            group = Group(id = GroupId(returnedId), groupName = "title", icon = KIcon.WorldStar, entryOrGroupCount = 2),
            entries = listOf(
                Group(id = GroupId(returnedSubGroupId), groupName = "cekla", icon = KIcon.Certificate, entryOrGroupCount = 0),
                Entry(EntryId(returnedEntryId), "cekla", "", KIcon.Clock)
            )
        )

        val group = sut.getGroup(GroupId(queryId))

        Assertions.assertEquals(expected, group)
    }

    @DisplayName("GIVEN no group WHEN requested THEN null is returned")
    @Test
    fun verifyNoGroup() = runBlocking {
        val queryId = UUID(0, 0)
        val group = sut.getGroup(GroupId(queryId))

        Assertions.assertEquals(null, group)
    }

    @DisplayName("GIVEN root group with subgroups and entries WHEN queried THEN it's returned parsed")
    @Test
    fun verifyRootGroupWithSubGroupsAndEntriesParsing() = runBlocking {
        val queryId = GroupId.ROOT_ID
        val returnedId = UUID(1, 1)

        val returnedSubGroupId = UUID(2, 2)
        val mockedSubGroupDom = mock<DomGroupWrapper>()
        whenever(mockedSubGroupDom.uuid).doReturn(returnedSubGroupId)
        whenever(mockedSubGroupDom.name).doReturn("cekla")
        val mockIcon = createMockIcon(KIcon.WorldStar.ordinal)
        whenever(mockedSubGroupDom.icon).doReturn(mockIcon)

        val returnedEntryId = UUID(3, 3)
        val mockedEntryDom = mock<DomEntryWrapper>()
        whenever(mockedEntryDom.uuid).doReturn(returnedEntryId)
        whenever(mockedEntryDom.title).doReturn("cekla")
        whenever(mockedEntryDom.username).doReturn("cekla-username")
        val mockIcon2 = createMockIcon(KIcon.Checked.ordinal)
        whenever(mockedEntryDom.icon).doReturn(mockIcon2)

        val mockedDom = mock<DomGroupWrapper>()
        whenever(mockedDom.uuid).doReturn(returnedId)
        whenever(mockedDom.name).doReturn("title")
        whenever(mockedDom.entries).doReturn(listOf(mockedEntryDom))
        whenever(mockedDom.groups).doReturn(listOf(mockedSubGroupDom))
        whenever(mockedDom.entriesCount).doReturn(1)
        whenever(mockedDom.groupsCount).doReturn(1)
        val mockIcon3 = createMockIcon(KIcon.Digicam.ordinal)
        whenever(mockedDom.icon).doReturn(mockIcon3)

        whenever(mockDatabase.rootGroup).thenReturn(mockedDom)

        val expected = GroupWithEntries(
            group = Group(id = GroupId(returnedId), groupName ="title", icon = KIcon.Digicam, entryOrGroupCount = 2),
            entries = listOf(
                Group(id = GroupId(returnedSubGroupId), groupName = "cekla", icon = KIcon.WorldStar),
                Entry(id = EntryId(returnedEntryId), entryName = "cekla", userName = "cekla-username", KIcon.Checked)
            )
        )

        val group = sut.getGroup(queryId)

        Assertions.assertEquals(expected, group)
    }

    @DisplayName("GIVEN throwing database THEN exception is thrown")
    @Test
    fun authenticationException() {
        whenever(mockDatabaseHolder.database).thenThrow(AuthenticationException())
        Assertions.assertThrows(AuthenticationException::class.java) {
            runBlocking { sut.getGroup(GroupId.ROOT_ID) }
        }
    }
}
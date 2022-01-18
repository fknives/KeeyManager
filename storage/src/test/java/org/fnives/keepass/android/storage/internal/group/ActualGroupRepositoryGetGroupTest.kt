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
        val mockedDoom = mock<DomGroupWrapper>()
        whenever(mockedDoom.uuid).doReturn(returnedId)
        whenever(mockedDoom.name).doReturn("title")
        whenever(mockedDoom.entries).doReturn(emptyList())
        whenever(mockedDoom.groups).doReturn(emptyList())
        whenever(mockDatabase.findGroup(queryId)).thenReturn(mockedDoom)
        val expected = GroupWithEntries(
            group = Group(GroupId(returnedId), "title", KIcon.OTHER),
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

        val mockedDom = mock<DomGroupWrapper>()
        whenever(mockedDom.uuid).doReturn(returnedId)
        whenever(mockedDom.name).doReturn("title")
        whenever(mockedDom.entries).doReturn(listOf(mockedEntryDom))
        whenever(mockedDom.groups).doReturn(emptyList())

        whenever(mockDatabase.findGroup(queryId)).thenReturn(mockedDom)

        val expected = GroupWithEntries(
            group = Group(GroupId(returnedId), "title", KIcon.OTHER),
            entries = listOf(Entry(id = EntryId(returnedEntryId), entryName ="cekla", userName = "cekla-2"))
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

        val mockedDom = mock<DomGroupWrapper>()
        whenever(mockedDom.uuid).doReturn(returnedId)
        whenever(mockedDom.name).doReturn("title")
        whenever(mockedDom.entries).doReturn(emptyList())
        whenever(mockedDom.groups).doReturn(listOf(mockedSubGroupDom))

        whenever(mockDatabase.findGroup(queryId)).thenReturn(mockedDom)

        val expected = GroupWithEntries(
            group = Group(GroupId(returnedId), "title", KIcon.OTHER),
            entries = listOf(Group(GroupId(returnedSubGroupId), "cekla", KIcon.OTHER))
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

        val returnedEntryId = UUID(3, 3)
        val mockedEntryDom = mock<DomEntryWrapper>()
        whenever(mockedEntryDom.uuid).doReturn(returnedEntryId)
        whenever(mockedEntryDom.title).doReturn("cekla")

        val mockedDom = mock<DomGroupWrapper>()
        whenever(mockedDom.uuid).doReturn(returnedId)
        whenever(mockedDom.name).doReturn("title")
        whenever(mockedDom.entries).doReturn(listOf(mockedEntryDom))
        whenever(mockedDom.groups).doReturn(listOf(mockedSubGroupDom))

        whenever(mockDatabase.findGroup(queryId)).thenReturn(mockedDom)

        val expected = GroupWithEntries(
            group = Group(GroupId(returnedId), "title", KIcon.OTHER),
            entries = listOf(
                Group(GroupId(returnedSubGroupId), "cekla", KIcon.OTHER),
                Entry(EntryId(returnedEntryId), "cekla", "")
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

        val returnedEntryId = UUID(3, 3)
        val mockedEntryDom = mock<DomEntryWrapper>()
        whenever(mockedEntryDom.uuid).doReturn(returnedEntryId)
        whenever(mockedEntryDom.title).doReturn("cekla")
        whenever(mockedEntryDom.username).doReturn("cekla-username")

        val mockedDom = mock<DomGroupWrapper>()
        whenever(mockedDom.uuid).doReturn(returnedId)
        whenever(mockedDom.name).doReturn("title")
        whenever(mockedDom.entries).doReturn(listOf(mockedEntryDom))
        whenever(mockedDom.groups).doReturn(listOf(mockedSubGroupDom))

        whenever(mockDatabase.rootGroup).thenReturn(mockedDom)

        val expected = GroupWithEntries(
            group = Group(GroupId(returnedId), "title", KIcon.OTHER),
            entries = listOf(
                Group(id = GroupId(returnedSubGroupId), groupName = "cekla", icon = KIcon.OTHER),
                Entry(id = EntryId(returnedEntryId), entryName = "cekla", userName = "cekla-username")
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
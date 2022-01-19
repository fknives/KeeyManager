package org.fnives.keepass.android.storage

import java.io.File
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.fnives.keepass.android.storage.internal.ActualKeePassRepository
import org.fnives.keepass.android.storage.model.Credentials
import org.fnives.keepass.android.storage.model.Entry
import org.fnives.keepass.android.storage.model.EntryDetailed
import org.fnives.keepass.android.storage.model.Group
import org.fnives.keepass.android.storage.model.GroupId
import org.fnives.keepass.android.storage.model.KIcon
import org.fnives.keepass.android.storage.testutil.TestDispatcherHolder
import org.fnives.keepass.android.storage.testutil.copyResource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchByUsernameIntegrationTest {

    private lateinit var sut: KeePassRepository
    private lateinit var testDispatcherHolder: TestDispatcherHolder
    private lateinit var databaseFile: File

    @BeforeEach
    fun setUp() {
        testDispatcherHolder = TestDispatcherHolder(startPaused = true)
        databaseFile = copyResource("empty.kdbx")
        sut = ActualKeePassRepository.getInstance(
            dispatcherHolder = testDispatcherHolder.dispatcherHolder
        )
        testDispatcherHolder.single.resumeDispatcher()
        runBlocking { sut.authenticate(Credentials(databaseFile, "test1")) }
    }

    @AfterEach
    fun tearDown() {
        databaseFile.delete()
    }

    @DisplayName("GIVEN empty database WHEN searching by username THEN nothing is returned")
    @Test
    fun emptyDBSearch() = runBlocking {
        val actual = sut.searchByUsername("blabla")

        Assertions.assertEquals(emptyList<Entry>(), actual)
    }

    @DisplayName("GIVEN nonExistentGroup WHEN searching by username THEN nothing is returned")
    @Test
    fun nonExistentGroupSearch() = runBlocking {

        val actual = sut.searchByUsername("blabla", scope = GroupId(UUID(2, 4)))

        Assertions.assertEquals(emptyList<Entry>(), actual)
    }

    @DisplayName("GIVEN flat matchin entry WHEN searching by username THEN nothing is returned")
    @Test
    fun flatMatchingSearch() = runBlocking {
        val entryID = sut.addEntry(
            EntryDetailed(entryName = "2", userName = "blabla", password = "", url = "", notes = ""),
            GroupId.ROOT_ID
        )
        val expected = listOf(Entry(id = entryID, entryName = "2", userName = "blabla", icon = KIcon.Key))

        val actual = sut.searchByUsername("blabla")

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN non matching entry WHEN searching by username THEN nothing is returned")
    @Test
    fun nonMatchingSearch() = runBlocking {
        sut.addEntry(
            EntryDetailed(entryName = "2", userName = "bla", password = "", url = "", notes = ""),
            GroupId.ROOT_ID
        )

        val actual = sut.searchByUsername("blabla")

        Assertions.assertEquals(emptyList<Entry>(), actual)
    }

    @DisplayName("GIVEN deep matching entry WHEN searching by username THEN nothing is returned")
    @Test
    fun deepMatchingSearch() = runBlocking {
        val matchingEntryId1 = sut.addEntry(
            EntryDetailed(entryName = "1", userName = "bla", password = "", url = "", notes = ""),
            GroupId.ROOT_ID
        )
        val groupId1 = sut.addGroup(Group(groupName = "a"), GroupId.ROOT_ID)
        val matchingEntryId2 = sut.addEntry(
            EntryDetailed(entryName = "2", userName = "aaabl", password = "", url = "", notes = ""),
            groupId1
        )
        val matchingEntryId3 = sut.addEntry(
            EntryDetailed(entryName = "3", userName = "aaablaaa", password = "", url = "", notes = ""),
            groupId1
        )
        val groupId2 = sut.addGroup(Group(groupName = "a"), groupId1)
        val matchingEntryId4 = sut.addEntry(
            EntryDetailed(entryName = "4", userName = "bbbblllll", password = "", url = "", notes = "", icon = KIcon.Digicam),
            groupId2
        )
        sut.addEntry(
            EntryDetailed(entryName = "5", userName = "b", password = "", url = "", notes = ""),
            groupId2
        )
        val expected = listOf(
            Entry(id = matchingEntryId1, entryName = "1", userName = "bla", icon = KIcon.Key),
            Entry(id = matchingEntryId2, entryName = "2", userName = "aaabl", icon = KIcon.Key),
            Entry(id = matchingEntryId3, entryName = "3", userName = "aaablaaa", icon = KIcon.Key),
            Entry(id = matchingEntryId4, entryName = "4", userName = "bbbblllll", icon = KIcon.Digicam),
        )

        val actual = sut.searchByUsername("bl")

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN deep matching entry WHEN searching by username THEN nothing is returned")
    @Test
    fun scopedMatchingSearch() = runBlocking {
        val matchingEntryId1 = sut.addEntry(
            EntryDetailed(entryName = "1", userName = "bla", password = "", url = "", notes = ""),
            GroupId.ROOT_ID
        )
        val groupId1 = sut.addGroup(Group(groupName = "a"), GroupId.ROOT_ID)
        val matchingEntryId2 = sut.addEntry(
            EntryDetailed(entryName = "2", userName = "aaabl", password = "", url = "", notes = ""),
            groupId1
        )
        val matchingEntryId3 = sut.addEntry(
            EntryDetailed(entryName = "3", userName = "aaablaaa", password = "", url = "", notes = ""),
            groupId1
        )
        val groupId2 = sut.addGroup(Group(groupName = "a"), groupId1)
        val matchingEntryId4 = sut.addEntry(
            EntryDetailed(entryName = "4", userName = "bbbblllll", password = "", url = "", notes = "", icon = KIcon.Digicam),
            groupId2
        )
        sut.addEntry(
            EntryDetailed(entryName = "5", userName = "b", password = "", url = "", notes = ""),
            groupId2
        )
        val expected = listOf(
            Entry(id = matchingEntryId2, entryName = "2", userName = "aaabl", icon = KIcon.Key),
            Entry(id = matchingEntryId3, entryName = "3", userName = "aaablaaa", icon = KIcon.Key),
            Entry(id = matchingEntryId4, entryName = "4", userName = "bbbblllll", icon = KIcon.Digicam),
        )

        val actual = sut.searchByUsername("bl", scope = groupId1)

        Assertions.assertEquals(expected, actual)
    }
}

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
import org.fnives.keepass.android.storage.model.GroupOrEntry
import org.fnives.keepass.android.storage.model.KIcon
import org.fnives.keepass.android.storage.testutil.TestDispatcherHolder
import org.fnives.keepass.android.storage.testutil.copyResource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchByNameIntegrationTest {

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

    @DisplayName("GIVEN empty database WHEN searching by name THEN nothing is returned")
    @Test
    fun emptyDBSearch() = runBlocking {

        val actual = sut.search("blabla")

        Assertions.assertEquals(emptyList<GroupOrEntry>(), actual)
    }

    @DisplayName("GIVEN nonExistentGroup WHEN searching by name THEN nothing is returned")
    @Test
    fun nonExistentGroupSearch() = runBlocking {
        val actual = sut.search("blabla", scope = GroupId(UUID(2, 4)))

        Assertions.assertEquals(emptyList<GroupOrEntry>(), actual)
    }

    @DisplayName("GIVEN database with non-matching group and entry WHEN searching by name THEN nothing is returned")
    @Test
    fun nonMatching() = runBlocking {
        sut.addGroup(Group(groupName = "1"), GroupId.ROOT_ID)
        sut.addEntry(EntryDetailed(entryName = "2", userName = "", password = "", url = "", notes = ""), GroupId.ROOT_ID)

        val actual = sut.search("blabla")

        Assertions.assertEquals(emptyList<GroupOrEntry>(), actual)
    }

    @DisplayName("GIVEN database with matching group and entry WHEN searching by name THEN they are returned")
    @Test
    fun matchingFlat() = runBlocking {
        val groupId = sut.addGroup(Group(groupName = "13"), GroupId.ROOT_ID)
        val entryId = sut.addEntry(EntryDetailed(entryName = "12", userName = "a", password = "", url = "", notes = ""), GroupId.ROOT_ID)
        val expected = listOf(
            Group(id = groupId, groupName = "13", icon = KIcon.Folder, entryOrGroupCount = 0),
            Entry(id = entryId, entryName = "12", icon = KIcon.Key, userName = "a")
        )

        val actual = sut.search("1")

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN entry matching by other attributes WHEN searching by name THEN nothing is returned")
    @Test
    fun matchingByOtherAttribute() = runBlocking {
        sut.addEntry(EntryDetailed(entryName = "xxx", userName = "12", password = "", url = "", notes = ""), GroupId.ROOT_ID)

        val actual = sut.search("1")

        Assertions.assertEquals(emptyList<GroupOrEntry>(), actual)
    }

    @DisplayName("GIVEN database with matching by other attribute entry WHEN searching by name THEN nothing is returned")
    @Test
    fun deepGroupAndEntrySearch() = runBlocking {
        val groupId = sut.addGroup(Group(groupName = "1"), GroupId.ROOT_ID)
        val groupToFind1Id = sut.addGroup(Group(groupName = "xxYYxx", icon = KIcon.FolderOpen), groupId)
        val subGroupId = sut.addGroup(Group(groupName = "1"), groupId)
        val entryToFind1 = sut.addEntry(
            EntryDetailed(entryName = "zzYYxx", userName = "12", password = "", url = "", notes = "", icon = KIcon.WorldStar),
            subGroupId
        )
        val subGroupId2 = sut.addGroup(Group(groupName = "1"), subGroupId)
        val groupToFind2Id = sut.addGroup(Group(groupName = "xxYYYxx"), subGroupId2)
        val entryToFind2 = sut.addEntry(
            EntryDetailed(entryName = "zzYYxx", userName = "123", password = "", url = "", notes = ""),
            groupToFind2Id
        )
        sut.addEntry(
            EntryDetailed(entryName = "a", userName = "124", password = "", url = "", notes = ""),
            groupToFind2Id
        )

        val expected = listOf(
            Group(id = groupToFind1Id, groupName = "xxYYxx", icon = KIcon.FolderOpen),
            Group(id = groupToFind2Id, groupName = "xxYYYxx", icon = KIcon.Folder, entryOrGroupCount = 2),
            Entry(id = entryToFind1, entryName = "zzYYxx", icon = KIcon.WorldStar, userName = "12"),
            Entry(id = entryToFind2, entryName = "zzYYxx", icon = KIcon.Key, userName = "123")
        )

        val actual = sut.search("yy")

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN database with matching by other attribute entry WHEN searching by name THEN nothing is returned")
    @Test
    fun deepScopedGroupAndEntrySearch() = runBlocking {
        val groupId = sut.addGroup(Group(groupName = "1"), GroupId.ROOT_ID)
        val groupToFind1Id = sut.addGroup(Group(groupName = "xxYYxx", icon = KIcon.FolderOpen), groupId)
        val subGroupId = sut.addGroup(Group(groupName = "1"), groupId)
        val entryToFind1 = sut.addEntry(
            EntryDetailed(entryName = "zzYYxx", userName = "12", password = "", url = "", notes = "", icon = KIcon.WorldStar),
            subGroupId
        )
        val subGroupId2 = sut.addGroup(Group(groupName = "1"), subGroupId)
        val groupToFind2Id = sut.addGroup(Group(groupName = "xxYYYxx"), subGroupId2)
        val entryToFind2 = sut.addEntry(
            EntryDetailed(entryName = "zzYYxx", userName = "123", password = "", url = "", notes = ""),
            groupToFind2Id
        )
        sut.addEntry(
            EntryDetailed(entryName = "a", userName = "124", password = "", url = "", notes = ""),
            groupToFind2Id
        )

        val expected = listOf(
            Entry(id = entryToFind2, entryName = "zzYYxx", icon = KIcon.Key, userName = "123")
        )

        val actual = sut.search("yy", scope = groupToFind2Id)

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN database with matching by other attribute entry WHEN searching by name THEN nothing is returned")
    @Test
    fun deepScopedGroupMultipleMatchAndEntrySearch() = runBlocking {
        val groupId = sut.addGroup(Group(groupName = "1"), GroupId.ROOT_ID)
        val groupToFind1Id = sut.addGroup(Group(groupName = "xxYYxx", icon = KIcon.FolderOpen), groupId)
        val subGroupId = sut.addGroup(Group(groupName = "1"), groupId)
        val entryToFind1 = sut.addEntry(
            EntryDetailed(entryName = "zzYYxx", userName = "12", password = "", url = "", notes = "", icon = KIcon.WorldStar),
            subGroupId
        )
        val subGroupId2 = sut.addGroup(Group(groupName = "1"), subGroupId)
        val groupToFind2Id = sut.addGroup(Group(groupName = "xxYYYxx"), subGroupId2)
        val entryToFind2 = sut.addEntry(
            EntryDetailed(entryName = "zzYYxx", userName = "123", password = "", url = "", notes = ""),
            groupToFind2Id
        )
        sut.addEntry(
            EntryDetailed(entryName = "a", userName = "124", password = "", url = "", notes = ""),
            groupToFind2Id
        )

        val expected = listOf(
            Group(id = groupToFind2Id, groupName = "xxYYYxx", icon = KIcon.Folder, entryOrGroupCount = 2),
            Entry(id = entryToFind2, entryName = "zzYYxx", icon = KIcon.Key, userName = "123")
        )

        val actual = sut.search("yy", scope = subGroupId2)

        Assertions.assertEquals(expected, actual)
    }
}

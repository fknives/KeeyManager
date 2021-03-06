package org.fnives.keepass.android.storage

import java.io.File
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.internal.ActualKeePassRepository
import org.fnives.keepass.android.storage.model.Credentials
import org.fnives.keepass.android.storage.model.Entry
import org.fnives.keepass.android.storage.model.EntryDetailed
import org.fnives.keepass.android.storage.model.EntryId
import org.fnives.keepass.android.storage.model.Group
import org.fnives.keepass.android.storage.model.GroupId
import org.fnives.keepass.android.storage.model.GroupWithEntries
import org.fnives.keepass.android.storage.model.KIcon
import org.fnives.keepass.android.storage.testutil.TestDispatcherHolder
import org.fnives.keepass.android.storage.testutil.copyResource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WritingIntegrationTest {

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
    }

    @AfterEach
    fun tearDown() {
        databaseFile.delete()
    }

    @DisplayName("GIVEN entry written WHEN accessed THEN it is returned")
    @Test
    fun writingEntryKeepsTheData() = runBlocking {
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val entryDetail = EntryDetailed(
            entryName = "my-special-entry",
            userName = "my",
            password = "special",
            url = "entry",
            notes = "uh",
            icon = KIcon.Apple,
        )
        val entryId = sut.addEntry(entryDetail, GroupId.ROOT_ID)

        val foundEntry = sut.getEntry(entryId)
        val foundInGroup = sut.getGroup(GroupId.ROOT_ID)?.entries?.filterIsInstance<Entry>()?.firstOrNull()

        val expectedEntryDetailed = entryDetail.copy(
            id = entryId, // provided by the repository
            lastModified = foundEntry?.lastModified ?: entryDetail.lastModified // time set is ignored by saving
        )
        val expectedEntry = Entry(
            id = entryId,
            entryName = entryDetail.entryName,
            userName = "my",
            icon = KIcon.Apple
        )
        Assertions.assertEquals(expectedEntryDetailed, foundEntry)
        Assertions.assertEquals(expectedEntry, foundInGroup)
    }

    @DisplayName("GIVEN group written WHEN accessed THEN it is returned")
    @Test
    fun writingGroupKeepsTheData() = runBlocking {
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val group = Group(groupName = "cica", icon = KIcon.FolderOpen)
        val groupId = sut.addGroup(group, GroupId.ROOT_ID)

        val subGroup = Group(groupName = "cica2", icon = KIcon.BlackBerry)
        val subGroupId = sut.addGroup(subGroup, groupId)

        val expectedGroup = group.copy(id = groupId, entryOrGroupCount = 1)
        val expectedSubGroup = subGroup.copy(id = subGroupId, entryOrGroupCount = 0)
        val expectedGroupWithEntries = GroupWithEntries(expectedGroup, listOf(expectedSubGroup))
        val expectedSubGroupWithEntries = GroupWithEntries(expectedSubGroup, emptyList())

        val foundGroup = sut.getGroup(groupId)
        val foundInRoot = sut.getGroup(GroupId.ROOT_ID)?.entries?.filterIsInstance<Group>()?.firstOrNull()
        val foundSubGroup = sut.getGroup(subGroupId)
        val foundSubGroupInGroup = sut.getGroup(groupId)?.entries?.filterIsInstance<Group>()?.firstOrNull()

        Assertions.assertEquals(expectedGroupWithEntries, foundGroup)
        Assertions.assertEquals(expectedGroup, foundInRoot)
        Assertions.assertEquals(expectedSubGroupWithEntries, foundSubGroup)
        Assertions.assertEquals(expectedSubGroup, foundSubGroupInGroup)
    }

    @DisplayName("GIVEN entry added to group WHEN accessed THEN it is returned")
    @Test
    fun writingEntryIntoGroupKeepsTheData() = runBlocking {
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val group = Group(groupName = "cica", icon = KIcon.Archive)
        val entryDetail = EntryDetailed(
            entryName = "my-special-entry",
            userName = "my",
            password = "special",
            url = "entry",
            notes = "uh"
        )
        val groupId = sut.addGroup(group, GroupId.ROOT_ID)
        val entryId = sut.addEntry(entryDetail, groupId)

        val expectedEntry = Entry(id = entryId, entryName = entryDetail.entryName, userName = "my", icon = KIcon.Key)
        val expectedGroup = group.copy(id = groupId, entryOrGroupCount = 1)
        val expectedGroupWithEntries = GroupWithEntries(expectedGroup, listOf(expectedEntry))

        val foundGroup = sut.getGroup(groupId)
        val foundInRoot = sut.getGroup(GroupId.ROOT_ID)?.entries?.filterIsInstance<Group>()?.firstOrNull()
        val foundEntry = sut.getEntry(entryId)
        val foundEntryInGroup = sut.getGroup(groupId)?.entries?.filterIsInstance<Entry>()?.firstOrNull()

        val expectedEntryDetailed = entryDetail.copy(
            id = entryId, // provided by the repository
            lastModified = foundEntry?.lastModified ?: entryDetail.lastModified // time set is ignored by saving
        )

        Assertions.assertEquals(expectedGroupWithEntries, foundGroup)
        Assertions.assertEquals(expectedGroup, foundInRoot)
        Assertions.assertEquals(expectedEntryDetailed, foundEntry)
        Assertions.assertEquals(expectedEntry, foundEntryInGroup)
    }

    @DisplayName("GIVEN written database WHEN copied then accessed THEN it still contains data")
    @Test
    fun writingIsPermanent() = runBlocking {
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val group = Group(groupName = "cica", icon = KIcon.Apple)
        val groupId = sut.addGroup(group, GroupId.ROOT_ID)
        val expectedGroup = group.copy(id = groupId)

        val copiedFile = File.createTempFile("copied-test-file", "kdbx")
        try {
            databaseFile.copyTo(copiedFile, overwrite = true)

            sut.authenticate(Credentials(copiedFile, "test1"))
            val actualGroupWithEntries = sut.getGroup(groupId)

            Assertions.assertEquals(expectedGroup, actualGroupWithEntries?.group)
        } finally {
            copiedFile.delete()
        }
    }

    @DisplayName("GIVEN subGroup WHEN writing subGroup into non existent group THEN exception is thrown")
    @Test
    fun addingSubGroupToNonExistentGroupThrows() = runBlocking<Unit> {
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val group = Group(groupName = "cica", icon = KIcon.FolderOpen)

        val expected = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlocking { sut.addGroup(group, GroupId(UUID(15000, 15000))) }
        }
        Assertions.assertEquals("Couldn't find parent to add group to", expected.message)
        Assertions.assertEquals(null, expected.cause)
    }

    @DisplayName("GIVEN subGroup WHEN writing subGroup into non existent group THEN exception is thrown")
    @Test
    fun addingEntryToNonExistentGroupThrows() = runBlocking {
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val entry = EntryDetailed(
            entryName = "entryName",
            userName = "userName",
            password = "password",
            url = "url",
            notes = "notes"
        )

        val expected = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlocking { sut.addEntry(entry, GroupId(UUID(15000, 15000))) }
        }
        Assertions.assertEquals("Couldn't find parent to add entry to", expected.message)
        Assertions.assertEquals(null, expected.cause)
    }

    @DisplayName("GIVEN multiple groups and entries written WHEN accessed THEN it is returned with correct counts")
    @Test
    fun writingMultipleGroupsAndEntries() = runBlocking {
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val group = Group(groupName = "cica", icon = KIcon.FolderOpen)
        val groupId = sut.addGroup(group, GroupId.ROOT_ID)
        val subGroup1 = Group(groupName = "subcica1", icon = KIcon.FolderOpen)
        sut.addGroup(subGroup1, groupId)
        val subGroup2 = Group(groupName = "subcica2", icon = KIcon.FolderOpen)
        sut.addGroup(subGroup2, groupId)
        val entry = EntryDetailed(entryName = "", userName = "", password = "", url = "", notes = "")
        sut.addEntry(entry, groupId)

        val expectedCount = 3

        val foundGroups = sut.getGroup(GroupId.ROOT_ID)?.entries.orEmpty().filterIsInstance<Group>()

        Assertions.assertEquals(1, foundGroups.size)
        Assertions.assertEquals(expectedCount, foundGroups.first().entryOrGroupCount)
    }

    @DisplayName("GIVEN unauthenticated DB WHEN adding THEN exception is thrown")
    @Test
    fun unauthenticated() {
        databaseFile = copyResource("empty.kdbx")
        val actualGroup = Assertions.assertThrows(AuthenticationException::class.java) {
            runBlocking { sut.addGroup(Group(id = GroupId(UUID(1, 1)), groupName = "alma"), GroupId.ROOT_ID) }
        }

        Assertions.assertEquals("Database is not initialized / authenticated", actualGroup.message)

        val actualEntry = Assertions.assertThrows(AuthenticationException::class.java) {
            runBlocking {
                val entryDetailed = EntryDetailed(
                    id = EntryId(UUID(1, 1)),
                    entryName = "",
                    userName = "",
                    password = "",
                    url = "",
                    notes = ""
                )
                sut.addEntry(entryDetailed, GroupId.ROOT_ID)
            }
        }

        Assertions.assertEquals("Database is not initialized / authenticated", actualEntry.message)
    }
}

package org.fnives.keepass.android.storage

import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.fnives.keepass.android.storage.internal.ActualKeePassRepository
import org.fnives.keepass.android.storage.model.Credentials
import org.fnives.keepass.android.storage.model.Entry
import org.fnives.keepass.android.storage.model.EntryDetailed
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

    @DisplayName("GIVEN_entry_written_WHEN_accessed_THEN_it_is_returned")
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
            icon = KIcon.OTHER,
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
            entryName = entryDetail.entryName
        )
        Assertions.assertEquals(expectedEntryDetailed, foundEntry)
        Assertions.assertEquals(expectedEntry, foundInGroup)
    }

    @DisplayName("GIVEN_group_written_WHEN_accessed_THEN_it_is_returned")
    @Test
    fun writingGroupKeepsTheData() = runBlocking {
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val group = Group(groupName = "cica", icon = KIcon.OTHER)
        val groupId = sut.addGroup(group, GroupId.ROOT_ID)
        val subGroup = Group(groupName = "cica2", icon = KIcon.OTHER)
        val subGroupId = sut.addGroup(subGroup, groupId)
        val expectedGroup = group.copy(id = groupId)
        val expectedSubGroup = subGroup.copy(id = subGroupId)
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

    @DisplayName("GIVEN_entry_added_to_group_WHEN_accessed_THEN_it_is_returned")
    @Test
    fun writingEntryIntoGroupKeepsTheData() = runBlocking {
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val group = Group(groupName = "cica", icon = KIcon.OTHER)
        val entryDetail = EntryDetailed(
            entryName = "my-special-entry",
            userName = "my",
            password = "special",
            url = "entry",
            notes = "uh",
            icon = KIcon.OTHER,
        )
        val groupId = sut.addGroup(group, GroupId.ROOT_ID)
        val entryId = sut.addEntry(entryDetail, groupId)

        val expectedEntry = Entry(id = entryId, entryName = entryDetail.entryName)
        val expectedGroup = group.copy(id = groupId)
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
        val group = Group(groupName = "cica", icon = KIcon.OTHER)
        val groupId = sut.addGroup(group, GroupId.ROOT_ID)
        val expectedGroup = group.copy(id = groupId)

        val copiedFile = File.createTempFile("copied-test-file","kdbx")
        try {
            databaseFile.copyTo(copiedFile, overwrite = true)

            sut.authenticate(Credentials(copiedFile, "test1"))
            val actualGroupWithEntries = sut.getGroup(groupId)

            Assertions.assertEquals(expectedGroup, actualGroupWithEntries?.group)
        } finally {
            copiedFile.delete()
        }
    }
}
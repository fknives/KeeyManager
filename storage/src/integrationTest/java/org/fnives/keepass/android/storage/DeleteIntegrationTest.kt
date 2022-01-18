package org.fnives.keepass.android.storage

import java.io.File
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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
class DeleteIntegrationTest {

    private lateinit var sut: KeePassRepository
    private lateinit var testDispatcherHolder: TestDispatcherHolder
    private lateinit var databaseFile: File

    @BeforeEach
    fun setUp() {
        testDispatcherHolder = TestDispatcherHolder(startPaused = true)
        sut = ActualKeePassRepository.getInstance(
            dispatcherHolder = testDispatcherHolder.dispatcherHolder
        )
    }

    @AfterEach
    fun tearDown() {
        databaseFile.delete()
    }

    @DisplayName("GIVEN_filled_database_WHEN_accessed_THEN_data_is_returned")
    @Test
    fun readingFilledDatabase() = runBlocking {
        databaseFile = copyResource("entry-in-group-recyclebin-off.kdbx")
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val expectedRootGroup = Group(
            id = GroupId.ROOT_ID,
            groupName = "Root",
            icon = KIcon.OTHER
        )
        val expectedGroup = Group(
            id = ENTRY_IN_GROUP_RECYCLEBIN_OFF_GROUP_ID,
            groupName = "cica",
            icon = KIcon.OTHER
        )
        val expectedRootGroupWithEntries = GroupWithEntries(group = expectedRootGroup, entries = listOf(expectedGroup))
        val expectedEntry = Entry(
            id = ENTRY_IN_GROUP_RECYCLEBIN_OFF_ENTRY_ID,
            entryName = "my-special-entry",
            userName = "my"
        )
        val expectedGroupWithEntries = GroupWithEntries(expectedGroup, listOf(expectedEntry))
        val expectedEntryDetail = EntryDetailed(
            id = ENTRY_IN_GROUP_RECYCLEBIN_OFF_ENTRY_ID,
            entryName = "my-special-entry",
            userName = "my",
            password = "special",
            url = "entry",
            notes = "uh",
            lastModified = Date(1642018057000),
            icon = KIcon.OTHER
        )

        val actualRootGroupWithEntries = sut.getGroup(GroupId.ROOT_ID)
        val actualGroup = sut.getGroup(expectedGroup.id)
        val foundEntry = sut.getEntry(expectedEntry.id)

        Assertions.assertEquals(expectedRootGroupWithEntries, actualRootGroupWithEntries)
        Assertions.assertEquals(expectedGroupWithEntries, actualGroup)
        Assertions.assertEquals(expectedEntryDetail, foundEntry)
    }

    @DisplayName("GIVEN_filled_database_WHEN_entry_is_deleted_THEN_it_is_no_longer_returned")
    @Test
    fun deletingEntry() = runBlocking {
        databaseFile = copyResource("entry-in-group-recyclebin-off.kdbx")
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val expectedGroup = Group(
            id = ENTRY_IN_GROUP_RECYCLEBIN_OFF_GROUP_ID,
            groupName = "cica",
            icon = KIcon.OTHER
        )
        val expectedGroupWithEntries = GroupWithEntries(expectedGroup, listOf())

        val entryId = EntryId(uuid = UUID.fromString("5838c71d-56a1-4a4d-b229-9e4a6562612f"))
        sut.deleteEntry(entryId)
        val actualGroup = sut.getGroup(expectedGroup.id)
        val foundEntry = sut.getEntry(entryId)

        Assertions.assertEquals(expectedGroupWithEntries, actualGroup)
        Assertions.assertEquals(null, foundEntry)
    }

    @DisplayName("GIVEN_filled_database_WHEN_entry_is_deleted_THEN_it_is_no_longer_returned")
    @Test
    fun deletingGroup() = runBlocking {
        databaseFile = copyResource("entry-in-group-recyclebin-off.kdbx")
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val expectedRootGroup = Group(
            id = GroupId.ROOT_ID,
            groupName = "Root",
            icon = KIcon.OTHER
        )
        val expectedRootGroupWithEntries = GroupWithEntries(expectedRootGroup, listOf())
        val entryId = ENTRY_IN_GROUP_RECYCLEBIN_OFF_ENTRY_ID
        val groupId = ENTRY_IN_GROUP_RECYCLEBIN_OFF_GROUP_ID

        sut.deleteGroup(groupId)
        val actualRootGroupWithEntries = sut.getGroup(GroupId.ROOT_ID)
        val actualGroup = sut.getGroup(groupId)
        val foundEntry = sut.getEntry(entryId)

        Assertions.assertEquals(expectedRootGroupWithEntries, actualRootGroupWithEntries)
        Assertions.assertEquals(actualGroup, foundEntry)
        Assertions.assertEquals(null, foundEntry)
    }

    @DisplayName("GIVEN written database which from we delete WHEN file is copied and accessed THEN data is still deleted")
    @Test
    fun deletingIsPermanent() = runBlocking {
        databaseFile = copyResource("entry-in-group-recyclebin-off.kdbx")
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val expectedRootGroup = Group(
            id = GroupId.ROOT_ID,
            groupName = "Root",
            icon = KIcon.OTHER
        )
        val expectedRootGroupWithEntries = GroupWithEntries(expectedRootGroup, listOf())
        val entryId = ENTRY_IN_GROUP_RECYCLEBIN_OFF_ENTRY_ID
        val groupId = ENTRY_IN_GROUP_RECYCLEBIN_OFF_GROUP_ID
        sut.deleteGroup(groupId)

        val copiedFile = File.createTempFile("copied-test-file", "kdbx")
        try {
            databaseFile.copyTo(copiedFile, overwrite = true)
            sut.authenticate(Credentials(copiedFile, "test1"))

            val actualRootGroupWithEntries = sut.getGroup(GroupId.ROOT_ID)
            val actualGroup = sut.getGroup(groupId)
            val foundEntry = sut.getEntry(entryId)

            Assertions.assertEquals(expectedRootGroupWithEntries, actualRootGroupWithEntries)
            Assertions.assertEquals(actualGroup, foundEntry)
            Assertions.assertEquals(null, foundEntry)
        } finally {
            copiedFile.delete()
        }
    }

    companion object {
        val ENTRY_IN_GROUP_RECYCLEBIN_OFF_ENTRY_ID = EntryId(uuid = UUID.fromString("5838c71d-56a1-4a4d-b229-9e4a6562612f"))
        val ENTRY_IN_GROUP_RECYCLEBIN_OFF_GROUP_ID = GroupId(uuid = UUID.fromString("09b083e9-472f-4385-9991-dac1abdef04c"))
    }
}
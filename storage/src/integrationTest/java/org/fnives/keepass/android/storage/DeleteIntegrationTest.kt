package org.fnives.keepass.android.storage

import java.io.File
import java.text.SimpleDateFormat
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

    @DisplayName("GIVEN filled database WHEN accessed THEN data is returned")
    @Test
    fun readingFilledDatabase() = runBlocking {
        databaseFile = copyResource("entry-in-group-recyclebin-off.kdbx")
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val expectedRootGroup = Group(
            id = GroupId.ROOT_ID,
            groupName = "Root",
            icon = KIcon.Folder,
            entryOrGroupCount = 1
        )
        val expectedGroup = Group(
            id = ENTRY_IN_GROUP_RECYCLEBIN_OFF_GROUP_ID,
            groupName = "cica",
            icon = KIcon.Key,
            entryOrGroupCount = 1
        )
        val expectedRootGroupWithEntries = GroupWithEntries(group = expectedRootGroup, entries = listOf(expectedGroup))
        val expectedEntry = Entry(
            id = ENTRY_IN_GROUP_RECYCLEBIN_OFF_ENTRY_ID,
            entryName = "my-special-entry",
            userName = "my",
            icon = KIcon.Warning
        )
        val expectedGroupWithEntries = GroupWithEntries(expectedGroup, listOf(expectedEntry))
        val expectedEntryDetail = EntryDetailed(
            id = ENTRY_IN_GROUP_RECYCLEBIN_OFF_ENTRY_ID,
            entryName = "my-special-entry",
            userName = "my",
            password = "special",
            url = "entry",
            notes = "uh",
            lastModified = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse("2022-01-12T22:07:37Z"),
            icon = KIcon.Warning
        )

        val actualRootGroupWithEntries = sut.getGroup(GroupId.ROOT_ID)
        val actualGroup = sut.getGroup(expectedGroup.id)
        val foundEntry = sut.getEntry(expectedEntry.id)

        Assertions.assertEquals(expectedRootGroupWithEntries, actualRootGroupWithEntries)
        Assertions.assertEquals(expectedGroupWithEntries, actualGroup)
        Assertions.assertEquals(expectedEntryDetail, foundEntry)
    }

    @DisplayName("GIVEN filled database WHEN entry is deleted THEN it is no longer returned")
    @Test
    fun deletingEntry() = runBlocking {
        databaseFile = copyResource("entry-in-group-recyclebin-off.kdbx")
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val expectedGroup = Group(
            id = ENTRY_IN_GROUP_RECYCLEBIN_OFF_GROUP_ID,
            groupName = "cica",
            icon = KIcon.Key,
            entryOrGroupCount = 0
        )
        val expectedGroupWithEntries = GroupWithEntries(expectedGroup, listOf())

        val entryId = EntryId(uuid = UUID.fromString("5838c71d-56a1-4a4d-b229-9e4a6562612f"))
        sut.deleteEntry(entryId)
        val actualGroup = sut.getGroup(expectedGroup.id)
        val foundEntry = sut.getEntry(entryId)

        Assertions.assertEquals(expectedGroupWithEntries, actualGroup)
        Assertions.assertEquals(null, foundEntry)
    }

    @DisplayName("GIVEN filled database WHEN entry is deleted THEN it is no longer returned")
    @Test
    fun deletingGroup() = runBlocking {
        databaseFile = copyResource("entry-in-group-recyclebin-off.kdbx")
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val expectedRootGroup = Group(
            id = GroupId.ROOT_ID,
            groupName = "Root",
            icon = KIcon.Folder,
            entryOrGroupCount = 0
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
            icon = KIcon.Folder,
            entryOrGroupCount = 0
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

    @DisplayName("GIVEN filled database WHEN deleting ROOT THEN exceptionIsThrown")
    @Test
    fun rootGroupCannotBeDeleted() = runBlocking {
        databaseFile = copyResource("entry-in-group-recyclebin-off.kdbx")
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))

        val expected = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlocking { sut.deleteGroup(GroupId.ROOT_ID) }
        }
        Assertions.assertEquals("Cannot delete the root Group", expected.message)
        Assertions.assertEquals(null, expected.cause)
    }

    @DisplayName("GIVEN unauthenticated DB WHEN deleting THEN exception is thrown")
    @Test
    fun unauthenticated() {
        databaseFile = copyResource("empty.kdbx")
        val actualGroup = Assertions.assertThrows(AuthenticationException::class.java) {
            runBlocking { sut.deleteGroup(GroupId(UUID(1, 1))) }
        }

        Assertions.assertEquals("Database is not initialized / authenticated", actualGroup.message)

        val actualEntry = Assertions.assertThrows(AuthenticationException::class.java) {
            runBlocking { sut.deleteEntry(EntryId(UUID(1, 1))) }
        }

        Assertions.assertEquals("Database is not initialized / authenticated", actualEntry.message)
    }

    companion object {
        val ENTRY_IN_GROUP_RECYCLEBIN_OFF_ENTRY_ID = EntryId(uuid = UUID.fromString("5838c71d-56a1-4a4d-b229-9e4a6562612f"))
        val ENTRY_IN_GROUP_RECYCLEBIN_OFF_GROUP_ID = GroupId(uuid = UUID.fromString("09b083e9-472f-4385-9991-dac1abdef04c"))
    }
}

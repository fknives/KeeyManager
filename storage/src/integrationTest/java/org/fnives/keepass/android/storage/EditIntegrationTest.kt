package org.fnives.keepass.android.storage

import java.io.File
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.fnives.keepass.android.storage.DeleteIntegrationTest.Companion.ENTRY_IN_GROUP_RECYCLEBIN_OFF_ENTRY_ID
import org.fnives.keepass.android.storage.DeleteIntegrationTest.Companion.ENTRY_IN_GROUP_RECYCLEBIN_OFF_GROUP_ID
import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.internal.ActualKeePassRepository
import org.fnives.keepass.android.storage.model.Credentials
import org.fnives.keepass.android.storage.model.Entry
import org.fnives.keepass.android.storage.model.EntryDetailed
import org.fnives.keepass.android.storage.model.EntryId
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
class EditIntegrationTest {

    private lateinit var sut: KeePassRepository
    private lateinit var testDispatcherHolder: TestDispatcherHolder
    private lateinit var databaseFile: File

    @BeforeEach
    fun setUp() {
        testDispatcherHolder = TestDispatcherHolder(startPaused = true)
        sut = ActualKeePassRepository.getInstance(
            dispatcherHolder = testDispatcherHolder.dispatcherHolder
        )
        databaseFile = copyResource("entry-in-group-recyclebin-off.kdbx")
    }

    @AfterEach
    fun tearDown() {
        databaseFile.delete()
    }

    @DisplayName("GIVEN group WHEN editing it's name THEN then group requested is edited")
    @Test
    fun editGroupName() = runBlocking {
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val expected = Group(
            id = ENTRY_IN_GROUP_RECYCLEBIN_OFF_GROUP_ID,
            groupName = "malaria",
            icon = KIcon.Key,
            entryOrGroupCount = 1
        )

        val groupWithEntries = sut.getGroup(ENTRY_IN_GROUP_RECYCLEBIN_OFF_GROUP_ID)
        Assertions.assertEquals("cica", groupWithEntries?.group?.groupName)
        sut.editGroup(groupWithEntries!!.group.copy(groupName = "malaria"))

        val actual = sut.getGroup(ENTRY_IN_GROUP_RECYCLEBIN_OFF_GROUP_ID)?.group

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN entry WHEN editing it's name and userName THEN then it's modified in it's parent group")
    @Test
    fun editEntryNameModifiesInGroup() = runBlocking {
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val expected = Entry(id = ENTRY_IN_GROUP_RECYCLEBIN_OFF_ENTRY_ID, entryName = "malaria", userName = "hush-hush", icon = KIcon.Key)

        val entries = sut.getGroup(ENTRY_IN_GROUP_RECYCLEBIN_OFF_GROUP_ID)?.entries.orEmpty().filterIsInstance<Entry>()
        Assertions.assertEquals(setOf("my-special-entry"), entries.map(Entry::entryName).toSet())
        Assertions.assertEquals(setOf("my"), entries.map(Entry::userName).toSet())
        val editEntry = EntryDetailed(
            id = ENTRY_IN_GROUP_RECYCLEBIN_OFF_ENTRY_ID,
            entryName = "malaria",
            userName = "hush-hush",
            password = "",
            url = "",
            notes = "",
            icon = KIcon.Key,
        )
        sut.editEntry(editEntry)

        val actual = sut.getGroup(ENTRY_IN_GROUP_RECYCLEBIN_OFF_GROUP_ID)?.entries.orEmpty().filterIsInstance<Entry>()

        Assertions.assertEquals(listOf(expected), actual)
    }

    @DisplayName("GIVEN entry WHEN editing entryDetail THEN then it's modified if accessed")
    @Test
    fun editEntryDetailModifiesAccessedEntry() = runBlocking {
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val expected = EntryDetailed(
            id = ENTRY_IN_GROUP_RECYCLEBIN_OFF_ENTRY_ID,
            entryName = "malaria",
            userName = "hush-hush",
            password = "hushy",
            url = "uri-mal",
            notes = "my-personal-notes",
            icon = KIcon.Apple,
        )
        val currentEntryDetail = EntryDetailed(
            id = ENTRY_IN_GROUP_RECYCLEBIN_OFF_ENTRY_ID,
            entryName = "my-special-entry",
            userName = "my",
            password = "special",
            url = "entry",
            notes = "uh",
            lastModified = Date(1642018057000),
            icon = KIcon.Warning
        )

        val currentEntry = sut.getEntry(ENTRY_IN_GROUP_RECYCLEBIN_OFF_ENTRY_ID)
        Assertions.assertEquals(currentEntryDetail, currentEntry)
        sut.editEntry(expected)

        val actual = sut.getEntry(ENTRY_IN_GROUP_RECYCLEBIN_OFF_ENTRY_ID)

        val expectedWithUpdatedDate = expected.copy(lastModified = actual?.lastModified ?: expected.lastModified)
        Assertions.assertEquals(expectedWithUpdatedDate, actual)
    }

    @DisplayName("GIVEN root group WHEN editing it's name THEN exception is thrown")
    @Test
    fun cannotEditRootGroup() = runBlocking<Unit> {
        testDispatcherHolder.single.resumeDispatcher()
        sut.authenticate(Credentials(databaseFile, "test1"))
        val rootGroupEdited = Group(
            id = GroupId.ROOT_ID,
            groupName = "malaria",
            icon = KIcon.Key
        )

        val expected = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlocking { sut.editGroup(rootGroupEdited) }
        }
        Assertions.assertEquals("Cannot edit the root Group", expected.message)
        Assertions.assertEquals(null, expected.cause)
    }

    @DisplayName("GIVEN unauthenticated DB WHEN editing THEN exception is thrown")
    @Test
    fun unauthenticated() {
        databaseFile = copyResource("empty.kdbx")
        val actualGroup = Assertions.assertThrows(AuthenticationException::class.java) {
            runBlocking { sut.editGroup(Group(id = GroupId(UUID(1, 1)), groupName = "alma")) }
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
                sut.editEntry(entryDetailed)
            }
        }

        Assertions.assertEquals("Database is not initialized / authenticated", actualEntry.message)
    }
}

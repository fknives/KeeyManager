package org.fnives.keepass.android.storage.internal.group

import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.internal.database.DatabaseHolder
import org.fnives.keepass.android.storage.internal.database.SavingDataBase
import org.fnives.keepass.android.storage.model.Group
import org.fnives.keepass.android.storage.model.GroupId
import org.fnives.keepass.android.storage.model.KIcon
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.linguafranca.pwdb.kdbx.dom.DomGroupWrapper
import org.linguafranca.pwdb.kdbx.dom.DomIconWrapper
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever

class ActualGroupRepositoryEditGroupTest {

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

    @DisplayName("GIVEN group and proper parent WHEN added THEN added to database properly")
    @Test
    fun editingGroupSuccess() = runBlocking {
        val groupId = GroupId(UUID(10000, 10000))
        val mockGroupDom = mock<DomGroupWrapper>()
        whenever(mockDatabase.findGroup(groupId.uuid)).doReturn(mockGroupDom)

        val mockIconDom = mock<DomIconWrapper>()
        whenever(mockDatabase.newIcon(KIcon.Checked.ordinal)).doReturn(mockIconDom)

        sut.editGroup(Group(id = groupId, groupName = "alma", icon = KIcon.Checked))

        val inOrder = inOrder(mockDatabase)
        inOrder.verify(mockDatabase).findGroup(groupId.uuid)
        inOrder.verify(mockDatabase).newIcon(KIcon.Checked.ordinal)
        inOrder.verify(mockDatabase).save()
        verifyNoMoreInteractions(mockDatabase)

        verify(mockGroupDom).name = "alma"
        verify(mockGroupDom).icon = mockIconDom
        verifyNoMoreInteractions(mockGroupDom)

        verifyZeroInteractions(mockIconDom)
    }

    @DisplayName("GIVEN root group WHEN edited THEN exception is thrown")
    @Test
    fun editingRootGroupThrows() {
        val groupId = GroupId.ROOT_ID

        val expected = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlocking { sut.editGroup(Group(id = groupId, groupName = "alma", icon = KIcon.Digicam)) }
        }

        Assertions.assertEquals("Cannot edit the root Group", expected.message)
        Assertions.assertEquals(null, expected.cause)
        verifyZeroInteractions(mockDatabase)
    }

    @DisplayName("GIVEN nonexistent group WHEN edited THEN exception is thrown")
    @Test
    fun editNonExistentGroupThrows() = runBlocking {
        val groupId = GroupId(UUID(10000, 10000))
        whenever(mockDatabase.findGroup(groupId.uuid)).doReturn(null)

        val expected = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlocking { sut.editGroup(Group(id = groupId, groupName = "alma", icon = KIcon.Digicam)) }
        }

        Assertions.assertEquals("Cannot edit non existent Group", expected.message)
        Assertions.assertEquals(null, expected.cause)
        verify(mockDatabase).findGroup(groupId.uuid)
        verifyNoMoreInteractions(mockDatabase)
    }

    @DisplayName("GIVEN unauthenticated database holder WHEN edited THEN exception is thrown")
    @Test
    fun unauthenticated() {
        whenever(mockDatabaseHolder.database).doThrow(AuthenticationException("Database is not initialized / authenticated"))

        val expected = Assertions.assertThrows(AuthenticationException::class.java) {
            runBlocking { sut.editGroup(Group(groupName = "alma", icon = KIcon.Digicam)) }
        }

        Assertions.assertEquals("Database is not initialized / authenticated", expected.message)
        Assertions.assertEquals(null, expected.cause)
    }
}
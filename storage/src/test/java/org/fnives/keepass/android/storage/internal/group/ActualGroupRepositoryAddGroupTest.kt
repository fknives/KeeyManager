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

class ActualGroupRepositoryAddGroupTest {

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
    fun addingGroupSuccess() = runBlocking {
        val expectedGroupId = GroupId(UUID(10000, 10000))
        val mockGroupDom = mock<DomGroupWrapper>()
        whenever(mockGroupDom.uuid).doReturn(expectedGroupId.uuid)

        val parentGroupId = GroupId(UUID(20000, 20000)) // notice
        val parentMockGroup = mock<DomGroupWrapper>()
        whenever(mockDatabase.findGroup(parentGroupId.uuid)).doReturn(parentMockGroup) // notice
        whenever(mockDatabase.newGroup()).doReturn(mockGroupDom)

        val mockIconDom = mock<DomIconWrapper>()
        whenever(mockDatabase.newIcon(KIcon.Digicam.ordinal)).doReturn(mockIconDom)

        val actual = sut.addGroup(Group(groupName = "alma", icon = KIcon.Digicam), parentGroupId)

        val inOrder = inOrder(mockDatabase, parentMockGroup)
        inOrder.verify(mockDatabase).findGroup(parentGroupId.uuid)
        inOrder.verify(mockDatabase).newGroup()
        inOrder.verify(mockDatabase).newIcon(KIcon.Digicam.ordinal)
        inOrder.verify(parentMockGroup).addGroup(mockGroupDom)
        inOrder.verify(mockDatabase).save()
        verifyNoMoreInteractions(mockDatabase)
        verifyNoMoreInteractions(parentMockGroup)

        verify(mockGroupDom).uuid
        verify(mockGroupDom).name = "alma"
        verify(mockGroupDom).icon = mockIconDom
        verifyNoMoreInteractions(mockGroupDom)

        verifyZeroInteractions(mockIconDom)

        Assertions.assertEquals(expectedGroupId, actual)
    }

    @DisplayName("GIVEN group and root parent WHEN added THEN added to database properly")
    @Test
    fun addingRootGroupSuccess() = runBlocking {
        val expectedGroupId = GroupId(UUID(10000, 10000))
        val mockGroupDom = mock<DomGroupWrapper>()
        whenever(mockGroupDom.uuid).doReturn(expectedGroupId.uuid)

        val parentGroupId = GroupId.ROOT_ID // notice
        val parentMockGroup = mock<DomGroupWrapper>()
        whenever(mockDatabase.rootGroup).doReturn(parentMockGroup) // notice
        whenever(mockDatabase.newGroup()).doReturn(mockGroupDom)

        val mockIconDom = mock<DomIconWrapper>()
        whenever(mockDatabase.newIcon(KIcon.Digicam.ordinal)).doReturn(mockIconDom)

        val actual = sut.addGroup(Group(groupName = "alma", icon = KIcon.Digicam), parentGroupId)

        val inOrder = inOrder(mockDatabase, parentMockGroup)
        inOrder.verify(mockDatabase).rootGroup
        inOrder.verify(mockDatabase).newGroup()
        inOrder.verify(mockDatabase).newIcon(KIcon.Digicam.ordinal)
        inOrder.verify(parentMockGroup).addGroup(mockGroupDom)
        inOrder.verify(mockDatabase).save()
        verifyNoMoreInteractions(mockDatabase)
        verifyNoMoreInteractions(parentMockGroup)

        verify(mockGroupDom).uuid
        verify(mockGroupDom).name = "alma"
        verify(mockGroupDom).icon = mockIconDom
        verifyNoMoreInteractions(mockGroupDom)

        verifyZeroInteractions(mockIconDom)

        Assertions.assertEquals(expectedGroupId, actual)
    }

    @DisplayName("GIVEN group and nonexistent parent WHEN added THEN exception is thrown")
    @Test
    fun addingToNonExistentParent() {
        val parentGroupId = GroupId(UUID(10000, 10000)) // notice
        whenever(mockDatabase.findGroup(parentGroupId.uuid)).doReturn(null) // notice

        val expected = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlocking { sut.addGroup(Group(groupName = "alma", icon = KIcon.Digicam), parentGroupId) }
        }

        Assertions.assertEquals("Couldn't find parent to add group to", expected.message)
        Assertions.assertEquals(null, expected.cause)
        verify(mockDatabase).findGroup(parentGroupId.uuid)
        verifyNoMoreInteractions(mockDatabase)
    }

    @DisplayName("GIVEN group and nonexistent root parent WHEN added THEN exception is thrown")
    @Test
    fun addingToNonExistentRootParent() {
        val parentGroupId = GroupId.ROOT_ID // notice
        whenever(mockDatabase.rootGroup).doReturn(null) // notice

        val expected = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlocking { sut.addGroup(Group(groupName = "alma", icon = KIcon.Digicam), parentGroupId) }
        }

        Assertions.assertEquals("Couldn't find parent to add group to", expected.message)
        Assertions.assertEquals(null, expected.cause)
        verify(mockDatabase).rootGroup
        verifyNoMoreInteractions(mockDatabase)
    }

    @DisplayName("GIVEN unauthenticated database holder WHEN added THEN exception is thrown")
    @Test
    fun unauthenticated() {
        whenever(mockDatabaseHolder.database).doThrow(AuthenticationException("Database is not initialized / authenticated"))

        val expected = Assertions.assertThrows(AuthenticationException::class.java) {
            runBlocking { sut.addGroup(Group(groupName = "alma", icon = KIcon.Digicam), GroupId.ROOT_ID) }
        }

        Assertions.assertEquals("Database is not initialized / authenticated", expected.message)
        Assertions.assertEquals(null, expected.cause)
    }
}

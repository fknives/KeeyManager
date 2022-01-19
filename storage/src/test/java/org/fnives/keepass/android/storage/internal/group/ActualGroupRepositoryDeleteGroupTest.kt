package org.fnives.keepass.android.storage.internal.group

import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.internal.database.DatabaseHolder
import org.fnives.keepass.android.storage.internal.database.SavingDataBase
import org.fnives.keepass.android.storage.model.GroupId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever

internal class ActualGroupRepositoryDeleteGroupTest {

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

    @DisplayName("GIVEN RootGroupId THEN exception is thrown")
    @Test
    fun rootGroupCannotBeDeleted() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlocking { sut.deleteGroup(GroupId.ROOT_ID) }
        }
    }

    @DisplayName("GIVEN throwing database THEN exception is thrown")
    @Test
    fun authenticationException() {
        whenever(mockDatabaseHolder.database).thenThrow(AuthenticationException())
        Assertions.assertThrows(AuthenticationException::class.java) {
            runBlocking { sut.deleteGroup(GroupId(UUID(0, 0))) }
        }
    }

    @DisplayName("GIVEN Normal Group WHEN deleted THEN delete is called")
    @Test
    fun groupDelete() {
        runBlocking {
            verifyZeroInteractions(mockDatabase)

            val groupID = GroupId(UUID.randomUUID())
            sut.deleteGroup(groupID)

            val inOrder = inOrder(mockDatabase)
            inOrder.verify(mockDatabase).deleteGroup(groupID.uuid)
            inOrder.verify(mockDatabase).save()
            inOrder.verifyNoMoreInteractions()
        }
    }
}

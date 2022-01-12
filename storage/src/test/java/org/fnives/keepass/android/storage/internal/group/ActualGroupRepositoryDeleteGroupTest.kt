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
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper
import org.mockito.kotlin.mock
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
            runBlocking { sut.deleteGroup(GroupId(UUID(0,0))) }
        }
    }
}
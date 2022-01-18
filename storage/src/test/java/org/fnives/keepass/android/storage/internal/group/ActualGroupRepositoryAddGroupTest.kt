package org.fnives.keepass.android.storage.internal.group

import org.fnives.keepass.android.storage.internal.database.DatabaseHolder
import org.fnives.keepass.android.storage.internal.database.SavingDataBase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
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

    @Test
    fun todo() {
        TODO()
    }
}
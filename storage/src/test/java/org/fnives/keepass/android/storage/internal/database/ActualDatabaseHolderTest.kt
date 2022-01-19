package org.fnives.keepass.android.storage.internal.database

import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

internal class ActualDatabaseHolderTest {

    private lateinit var sut: ActualDatabaseHolder

    @BeforeEach
    fun setUp() {
        sut = ActualDatabaseHolder()
    }

    @DisplayName("WHEN initialized THEN _database is NULL")
    @Test
    fun verifyByDefaultDatabaseIsNull() {
        Assertions.assertEquals(null, sut._database)
    }

    @DisplayName("WHEN initialized THEN database throws")
    @Test
    fun verifyByDefaultDatabaseThrows() {
        Assertions.assertThrows(AuthenticationException::class.java) {
            sut.database
        }
    }

    @DisplayName("GIVEN set database WHEN requested THEN database is returned")
    @Test
    fun verifyDatabaseIsReturnedWhenSet() {
        val mockDatabase = mock<SavingDataBase>()
        sut._database = mockDatabase

        Assertions.assertEquals(mockDatabase, sut.database)
    }
}

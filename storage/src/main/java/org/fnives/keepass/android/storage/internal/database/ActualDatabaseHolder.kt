package org.fnives.keepass.android.storage.internal.database

import org.fnives.keepass.android.storage.exception.AuthenticationException

internal class ActualDatabaseHolder : DatabaseHolder {

    @Suppress("PropertyName")
    var _database: SavingDataBase? = null

    override val database: SavingDataBase
        get() = _database ?: throw AuthenticationException("Database is not initialized / authenticated")
}
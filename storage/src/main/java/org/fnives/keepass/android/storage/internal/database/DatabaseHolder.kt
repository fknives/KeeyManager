package org.fnives.keepass.android.storage.internal.database

import org.fnives.keepass.android.storage.exception.AuthenticationException

internal interface DatabaseHolder {
    @get:Throws(AuthenticationException::class)
    val database: SavingDataBase
}

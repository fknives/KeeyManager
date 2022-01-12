package org.fnives.keepass.android.storage.internal.database

import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper

internal class ActualKPDatabaseHolder: KPDatabaseHolder {

    @Suppress("PropertyName")
    var _database: DomDatabaseWrapper? = null

    override val database: DomDatabaseWrapper get() = _database ?: throw AuthenticationException()
}
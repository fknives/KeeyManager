package org.fnives.keepass.android.storage.internal.database

import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper

internal interface KPDatabaseHolder {
    @get:Throws(AuthenticationException::class)
    val database: DomDatabaseWrapper
}
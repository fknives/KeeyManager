package org.fnives.keepass.android.storage.internal.database

import org.linguafranca.pwdb.Database
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper
import org.linguafranca.pwdb.kdbx.dom.DomEntryWrapper
import org.linguafranca.pwdb.kdbx.dom.DomGroupWrapper
import org.linguafranca.pwdb.kdbx.dom.DomIconWrapper

internal interface SavingDataBase : Database<DomDatabaseWrapper, DomGroupWrapper, DomEntryWrapper, DomIconWrapper> {

    suspend fun save()
}
package org.fnives.keepass.android.storage.internal.database

import java.io.OutputStream
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.linguafranca.pwdb.Credentials
import org.linguafranca.pwdb.Database
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper
import org.linguafranca.pwdb.kdbx.dom.DomEntryWrapper
import org.linguafranca.pwdb.kdbx.dom.DomGroupWrapper
import org.linguafranca.pwdb.kdbx.dom.DomIconWrapper

internal class SavingDomDatabaseWrapper(
    private val credentials: Credentials,
    private val delegate: Database<DomDatabaseWrapper, DomGroupWrapper, DomEntryWrapper, DomIconWrapper>,
    private val outputStreamFactory: () -> OutputStream,
    private val dispatcher: CoroutineDispatcher
) : SavingDataBase,
    Database<DomDatabaseWrapper, DomGroupWrapper, DomEntryWrapper, DomIconWrapper> by delegate {

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun save() = withContext(dispatcher) {
        save(credentials, outputStreamFactory())
    }

    companion object {
        fun DomDatabaseWrapper.wrapIntoAutoSaving(
            credentials: Credentials,
            outputStreamFactory: () -> OutputStream,
            dispatcher: CoroutineDispatcher
        ) =
            SavingDomDatabaseWrapper(
                credentials,
                this@wrapIntoAutoSaving,
                outputStreamFactory,
                dispatcher
            )
    }
}
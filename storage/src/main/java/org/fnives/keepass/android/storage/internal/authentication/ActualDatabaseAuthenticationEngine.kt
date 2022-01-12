package org.fnives.keepass.android.storage.internal.authentication

import kotlinx.coroutines.withContext
import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.internal.database.ActualDatabaseHolder
import org.fnives.keepass.android.storage.internal.database.SavingDomDatabaseWrapper.Companion.wrapIntoAutoSaving
import org.fnives.keepass.android.storage.internal.util.DispatcherHolder
import org.fnives.keepass.android.storage.log.Logging
import org.fnives.keepass.android.storage.model.Credentials
import org.linguafranca.pwdb.kdbx.KdbxCreds
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper

internal class ActualDatabaseAuthenticationEngine(
    private val databaseHolder: ActualDatabaseHolder,
    private val dispatcherHolder: DispatcherHolder
) : DatabaseAuthenticationEngine {

    override suspend fun authenticate(credentials: Credentials) {
        val kdbxCredentials = KdbxCreds(credentials.password.toByteArray())

        try {
            withContext(dispatcherHolder.single) {
                @Suppress("BlockingMethodInNonBlockingContext")
                databaseHolder._database = DomDatabaseWrapper.load(kdbxCredentials, credentials.databaseInputStream)
                    .wrapIntoAutoSaving(
                        kdbxCredentials,
                        credentials.databaseOutputStreamFactory,
                        dispatcherHolder.single
                    )

            }
        } catch (throwable: Throwable) {
            Logging.log(throwable)
            throw AuthenticationException(cause = throwable)
        }
    }

    override suspend fun disconnect() {
        withContext(dispatcherHolder.single) {
            databaseHolder._database?.save()
            databaseHolder._database = null
        }
    }
}
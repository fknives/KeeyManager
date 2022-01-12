package org.fnives.keepass.android.storage.internal.authentication

import kotlinx.coroutines.withContext
import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.internal.database.ActualKPDatabaseHolder
import org.fnives.keepass.android.storage.internal.util.DispatcherHolder
import org.fnives.keepass.android.storage.log.Logging
import org.fnives.keepass.android.storage.model.Credentials
import org.linguafranca.pwdb.kdbx.KdbxCreds
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper

internal class ActualDatabaseAuthenticationEngine(
    private val databaseHolder: ActualKPDatabaseHolder,
    private val dispatcherHolder: DispatcherHolder
) : DatabaseAuthenticationEngine {

    override suspend fun authenticate(credentials: Credentials) {
        val kdbxCredentials = KdbxCreds(credentials.password.toByteArray())

        try {
            withContext(dispatcherHolder.single) {
                @Suppress("BlockingMethodInNonBlockingContext")
                databaseHolder._database = DomDatabaseWrapper.load(kdbxCredentials, credentials.databaseInputStream)
            }
        } catch (throwable: Throwable) {
            Logging.log(throwable)
            throw AuthenticationException(cause = throwable)
        }
    }

    override suspend fun disconnect() {
        withContext(dispatcherHolder.single) {
            databaseHolder._database = null
        }
    }
}
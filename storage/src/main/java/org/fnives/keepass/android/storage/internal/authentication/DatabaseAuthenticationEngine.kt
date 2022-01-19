package org.fnives.keepass.android.storage.internal.authentication

import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.model.Credentials

internal interface DatabaseAuthenticationEngine {

    @Throws(AuthenticationException::class)
    suspend fun authenticate(credentials: Credentials)

    suspend fun disconnect()
}

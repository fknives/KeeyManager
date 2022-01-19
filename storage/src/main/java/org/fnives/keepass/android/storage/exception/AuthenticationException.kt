package org.fnives.keepass.android.storage.exception

class AuthenticationException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)

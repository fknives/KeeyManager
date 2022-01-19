package org.fnives.keepass.android.storage.log

interface Logger {

    fun log(message: String)

    fun log(error: Throwable)
}

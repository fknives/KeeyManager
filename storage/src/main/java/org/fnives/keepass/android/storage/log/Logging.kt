package org.fnives.keepass.android.storage.log

object Logging : Logger {

    var actualLogger: Logger? = null

    override fun log(error: Throwable) {
        actualLogger?.log(error)
    }

    override fun log(message: String) {
        actualLogger?.log(message)
    }
}
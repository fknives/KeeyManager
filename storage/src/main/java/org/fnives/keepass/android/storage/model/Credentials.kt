package org.fnives.keepass.android.storage.model

import java.io.File
import java.io.InputStream
import java.io.OutputStream

class Credentials(
    val databaseInputStream: InputStream,
    val databaseOutputStreamFactory: () -> OutputStream,
    val password: String
) {
    constructor(
        databaseFile: File,
        password: String
    ) : this(
        databaseInputStream = databaseFile.inputStream(),
        databaseOutputStreamFactory = { databaseFile.outputStream() },
        password = password,
    )
}

package org.fnives.keepass.android.storage.model

import java.io.File
import java.io.InputStream

class Credentials(
    val databaseInputStream: InputStream,
    val password: String
) {

    constructor(file: File, password: String) : this(file.inputStream(), password)
}
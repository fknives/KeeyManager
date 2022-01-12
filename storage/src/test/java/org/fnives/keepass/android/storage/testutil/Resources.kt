package org.fnives.keepass.android.storage.testutil

import java.io.File
import java.io.InputStream

fun Any.resourceAsStream(path: String) : InputStream =
    javaClass.classLoader.getResourceAsStream(path)!!

fun Any.copyResource(path: String) : File{
    val resource = javaClass.classLoader.getResourceAsStream(path)!!
    val file = File.createTempFile("testing-file","kdbx")
    resource.copyTo(file.outputStream())
    return file
}
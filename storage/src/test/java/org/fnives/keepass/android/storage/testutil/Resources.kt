package org.fnives.keepass.android.storage.testutil

import java.io.InputStream

fun Any.resourceAsStream(path: String) : InputStream =
    javaClass.classLoader.getResourceAsStream(path)!!
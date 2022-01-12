package org.fnives.keepass.android.storage.internal.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext

class DispatcherHolder(
    val main: CoroutineDispatcher = Dispatchers.Main,
    val io: CoroutineDispatcher = Dispatchers.IO,
    val single: CoroutineDispatcher = newSingleThreadContext("Storage-Single-Thread")
)
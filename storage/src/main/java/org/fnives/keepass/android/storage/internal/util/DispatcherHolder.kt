package org.fnives.keepass.android.storage.internal.util

import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher

@OptIn(ObsoleteCoroutinesApi::class)
internal class DispatcherHolder constructor(
    val main: CoroutineDispatcher = Dispatchers.Main,
    val io: CoroutineDispatcher = Dispatchers.IO,
    val single: CoroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
)
package org.fnives.keepass.android.storage.testutil

import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.fnives.keepass.android.storage.internal.util.DispatcherHolder

class TestDispatcherHolder(startPaused: Boolean = false) {
    val main = TestCoroutineDispatcher()
    val io = TestCoroutineDispatcher()
    val single = TestCoroutineDispatcher()

    init {
        if (startPaused) {
            pauseAll()
        }
    }

    fun pauseAll() {
        main.pauseDispatcher()
        io.pauseDispatcher()
        single.pauseDispatcher()
    }

    val dispatcherHolder = DispatcherHolder(
        main = main,
        io = io,
        single = single
    )
}
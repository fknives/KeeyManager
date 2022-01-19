package org.fnives.keepass.android.storage.testutil

import org.linguafranca.pwdb.kdbx.dom.DomIconWrapper
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

fun createMockIcon(index: Int) = mock<DomIconWrapper>().apply {
    whenever(this.index).doReturn(index)
}

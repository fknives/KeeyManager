package org.fnives.keepass.android.storage.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class GroupTest {

    @Test
    fun givenRootIdThenIsRootTrue() {
        val group = Group(GroupId.ROOT_ID,"",KIcon.Digicam)

        Assertions.assertTrue(group.isRoot)
    }

    @Test
    fun givenEqualGroupIdToRootThenIsRootTrue() {
        val group = Group(GroupId.ROOT_ID.copy(),"",KIcon.Digicam)

        Assertions.assertFalse(group.isRoot)
    }
}

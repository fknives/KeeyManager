package org.fnives.keepass.android.storage.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class GroupTest {

    @Test
    fun givenRootIdThenIsRootTrue() {
        val group = Group(id = GroupId.ROOT_ID, groupName = "")

        Assertions.assertTrue(group.isRoot)
    }

    @Test
    fun givenEqualGroupIdToRootThenIsRootTrue() {
        val group = Group(id = GroupId.ROOT_ID.copy(), groupName = "")

        Assertions.assertFalse(group.isRoot)
    }
}

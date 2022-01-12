package org.fnives.keepass.android.storage.model

import java.util.UUID

/**
 * Unify id of an Entry
 */
data class GroupId(val uuid: UUID) {

    companion object {
        internal val GENERATE_ID = GroupId(UUID(0,0))
        val ROOT_ID = GroupId(UUID(0,0))
    }
}
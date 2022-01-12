package org.fnives.keepass.android.storage.model

import java.util.UUID

/**
 * Unify id of an Entry
 */
@JvmInline
value class GroupId(val uuid: UUID) {

    companion object {
        val GENERATE_ID = GroupId(UUID(0,0))
    }
}
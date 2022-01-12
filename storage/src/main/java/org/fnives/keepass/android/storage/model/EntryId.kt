package org.fnives.keepass.android.storage.model

import java.util.UUID

/**
 * Unify id of an Entry
 */
@JvmInline
value class EntryId(val uuid: UUID) {

    companion object {
        val GENERATE_ID = EntryId(UUID(0,0))
    }
}
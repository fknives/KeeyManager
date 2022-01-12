package org.fnives.keepass.android.storage.model

import java.util.UUID

@JvmInline
value class EntryId(val uuid: UUID) {

    companion object {
        val GENERATE_ID = EntryId(UUID(0,0))
    }
}
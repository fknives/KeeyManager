package org.fnives.keepass.android.storage.internal.entry

import org.fnives.keepass.android.storage.model.EntryDetailed
import org.fnives.keepass.android.storage.model.EntryId
import org.fnives.keepass.android.storage.model.GroupId

class ActualEntryRepository : EntryRepository {
    override suspend fun getEntry(entryId: EntryId): EntryDetailed {
        TODO("Not yet implemented")
    }

    override suspend fun addEntry(entry: EntryDetailed, groupId: GroupId) {
        TODO("Not yet implemented")
    }

    override suspend fun editEntry(entryId: EntryId, entry: EntryDetailed) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteEntry(entryId: EntryId) {
        TODO("Not yet implemented")
    }
}
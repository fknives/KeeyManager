package org.fnives.keepass.android.storage.internal.entry

import org.fnives.keepass.android.storage.internal.IconConverter
import org.fnives.keepass.android.storage.internal.database.DatabaseHolder
import org.fnives.keepass.android.storage.internal.database.getDomById
import org.fnives.keepass.android.storage.model.EntryDetailed
import org.fnives.keepass.android.storage.model.EntryId
import org.fnives.keepass.android.storage.model.GroupId

internal class ActualEntryRepository(
    private val databaseHolder: DatabaseHolder,
    private val entryConverter: EntryConverter = EntryConverter(IconConverter(databaseHolder))
) : EntryRepository {

    private val database get() = databaseHolder.database

    override suspend fun getEntry(entryId: EntryId): EntryDetailed? {
        val dom = database.findEntry(entryId.uuid) ?: return null

        return entryConverter.convert(dom)
    }

    override suspend fun addEntry(entry: EntryDetailed, parentId: GroupId): EntryId {
        val parentDom = database.getDomById(parentId)
            ?: throw IllegalArgumentException("Couldn't find parent to add entry to")
        val dom = database.newEntry()
        entryConverter.copyTo(from = entry, to = dom)
        parentDom.addEntry(dom)
        database.save()

        return entryConverter.convertToId(dom)
    }

    override suspend fun editEntry(entry: EntryDetailed) {
        val dom = database.findEntry(entry.id.uuid) ?: return
        entryConverter.copyTo(from = entry, to = dom)
        database.save()
    }

    override suspend fun deleteEntry(entryId: EntryId) {
        database.deleteEntry(entryId.uuid)
        database.save()
    }
}
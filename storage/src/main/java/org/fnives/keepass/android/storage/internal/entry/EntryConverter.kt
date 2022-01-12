package org.fnives.keepass.android.storage.internal.entry

import org.fnives.keepass.android.storage.model.EntryDetailed
import org.fnives.keepass.android.storage.model.EntryId
import org.fnives.keepass.android.storage.model.KIcon
import org.linguafranca.pwdb.kdbx.dom.DomEntryWrapper

class EntryConverter {

    fun copyTo(from: EntryDetailed, to: DomEntryWrapper) {
        to.title = from.entryName
        to.username = from.userName
        to.notes = from.notes
        to.password = from.password
        to.url = from.url
        // todo to.icon = from.icon.ordinal
    }

    fun convertToId(dom: DomEntryWrapper): EntryId = EntryId(dom.uuid)

    fun convert(dom: DomEntryWrapper): EntryDetailed =
        EntryDetailed(
            id = EntryId(dom.uuid),
            entryName = dom.title,
            userName = dom.username,
            password = dom.password,
            url = dom.url,
            notes = dom.notes,
            lastModified = dom.lastModificationTime,
            icon = KIcon.OTHER // todo
        )
}
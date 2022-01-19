package org.fnives.keepass.android.storage.internal.entry

import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.internal.IconConverter
import org.fnives.keepass.android.storage.model.EntryDetailed
import org.fnives.keepass.android.storage.model.EntryId
import org.linguafranca.pwdb.kdbx.dom.DomEntryWrapper

internal class EntryConverter(private val iconConverter: IconConverter) {

    @Throws(AuthenticationException::class)
    fun copyTo(from: EntryDetailed, to: DomEntryWrapper) {
        to.title = from.entryName
        to.username = from.userName
        to.notes = from.notes
        to.password = from.password
        to.url = from.url
        to.icon = iconConverter.convert(from.icon)
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
            icon = iconConverter.convert(dom.icon)
        )
}

package org.fnives.keepass.android.storage.internal.group

import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.internal.IconConverter
import org.fnives.keepass.android.storage.model.Entry
import org.fnives.keepass.android.storage.model.EntryId
import org.fnives.keepass.android.storage.model.Group
import org.fnives.keepass.android.storage.model.GroupId
import org.linguafranca.pwdb.kdbx.dom.DomEntryWrapper
import org.linguafranca.pwdb.kdbx.dom.DomGroupWrapper

internal class GroupConverter(private val iconConverter: IconConverter) {

    fun convert(domGroupWrapper: DomGroupWrapper): Group =
        Group(
            id = if (domGroupWrapper.isRootGroup) GroupId.ROOT_ID else GroupId(domGroupWrapper.uuid),
            groupName = domGroupWrapper.name,
            entryOrGroupCount = domGroupWrapper.entriesCount + domGroupWrapper.groupsCount,
            icon = iconConverter.convert(domGroupWrapper.icon)
        )

    fun convert(domEntryWrapper: DomEntryWrapper): Entry =
        Entry(
            id = EntryId(domEntryWrapper.uuid),
            entryName = domEntryWrapper.title,
            userName = domEntryWrapper.username.orEmpty(),
            icon = iconConverter.convert(domEntryWrapper.icon)
        )

    @Throws(AuthenticationException::class)
    fun copyTo(from: Group, to: DomGroupWrapper) {
        to.name = from.groupName
        to.icon = iconConverter.convert(from.icon)
    }

    fun convertToId(dom: DomGroupWrapper): GroupId = GroupId(dom.uuid)
}
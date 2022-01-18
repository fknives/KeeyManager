package org.fnives.keepass.android.storage.internal.group

import org.fnives.keepass.android.storage.model.Entry
import org.fnives.keepass.android.storage.model.EntryId
import org.fnives.keepass.android.storage.model.Group
import org.fnives.keepass.android.storage.model.GroupId
import org.fnives.keepass.android.storage.model.KIcon
import org.linguafranca.pwdb.kdbx.dom.DomEntryWrapper
import org.linguafranca.pwdb.kdbx.dom.DomGroupWrapper

class GroupConverter {

    fun convert(domGroupWrapper: DomGroupWrapper): Group =
        Group(
            id = if (domGroupWrapper.isRootGroup) GroupId.ROOT_ID else GroupId(domGroupWrapper.uuid),
            groupName = domGroupWrapper.name,
            icon = KIcon.OTHER
        )

    fun convert(domEntryWrapper: DomEntryWrapper): Entry =
        Entry(
            id = EntryId(domEntryWrapper.uuid),
            entryName = domEntryWrapper.title,
            userName = domEntryWrapper.username.orEmpty()
        )

    fun copyTo(from: Group, to: DomGroupWrapper) {
        to.name = from.groupName
        // todo to.icon = from.icon
    }

    fun convertToId(dom: DomGroupWrapper): GroupId = GroupId(dom.uuid)
}
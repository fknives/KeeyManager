package org.fnives.keepass.android.storage.internal.group

import org.fnives.keepass.android.storage.internal.IconConverter
import org.fnives.keepass.android.storage.internal.database.DatabaseHolder
import org.fnives.keepass.android.storage.internal.database.getDomById
import org.fnives.keepass.android.storage.model.Group
import org.fnives.keepass.android.storage.model.GroupId
import org.fnives.keepass.android.storage.model.GroupWithEntries

internal class ActualGroupRepository(
    private val databaseHolder: DatabaseHolder,
    private val groupConverter: GroupConverter = GroupConverter(IconConverter(databaseHolder))
) : GroupRepository {

    private val database get() = databaseHolder.database

    override suspend fun getGroup(groupId: GroupId): GroupWithEntries? {
        val domGroupWrapper = database.getDomById(groupId)

        val group = domGroupWrapper?.let(groupConverter::convert) ?: return null
        val entries = domGroupWrapper.entries.orEmpty().map(groupConverter::convert)
        val subGroups = domGroupWrapper.groups.orEmpty().map(groupConverter::convert)

        return GroupWithEntries(
            group = group,
            entries = subGroups.plus(entries)
        )
    }

    override suspend fun addGroup(group: Group, parentId: GroupId): GroupId {
        val parentDom = database.getDomById(parentId) ?: throw IllegalArgumentException("Couldn't find parent")
        val domGroupWrapper = database.newGroup()
        groupConverter.copyTo(from = group, to = domGroupWrapper)
        parentDom.addGroup(domGroupWrapper)
        database.save()

        return groupConverter.convertToId(domGroupWrapper)
    }

    override suspend fun editGroup(group: Group) {
        if (group.id === GroupId.ROOT_ID) {
            throw IllegalArgumentException("Cannot delete the root Group")
        }
        val domGroupWrapper = database.getDomById(group.id) ?: return
        groupConverter.copyTo(from = group, to = domGroupWrapper)
        database.save()
    }

    override suspend fun deleteGroup(groupId: GroupId) {
        if (groupId === GroupId.ROOT_ID) {
            throw IllegalArgumentException("Cannot delete the root Group")
        }
        database.deleteGroup(groupId.uuid)
        database.save()
    }
}
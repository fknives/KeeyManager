package org.fnives.keepass.android.storage.internal.group

import org.fnives.keepass.android.storage.model.Group
import org.fnives.keepass.android.storage.model.GroupId
import org.fnives.keepass.android.storage.model.GroupWithEntries

internal class ActualGroupRepository : GroupRepository {
    override suspend fun getGroup(groupId: GroupId): GroupWithEntries {
        TODO("Not yet implemented")
    }

    override suspend fun addGroup(group: Group) {
        TODO("Not yet implemented")
    }

    override suspend fun editGroup(groupId: GroupId, group: Group) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGroup(groupId: GroupId) {
        TODO("Not yet implemented")
    }
}
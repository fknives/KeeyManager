package org.fnives.keepass.android.storage.internal.group

import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.model.Group
import org.fnives.keepass.android.storage.model.GroupId
import org.fnives.keepass.android.storage.model.GroupWithEntries

internal interface GroupRepository {

    @Throws(AuthenticationException::class)
    suspend fun getGroup(groupId: GroupId): GroupWithEntries?

    @Throws(AuthenticationException::class, IllegalArgumentException::class)
    suspend fun addGroup(group: Group, parentId: GroupId): GroupId

    @Throws(AuthenticationException::class, IllegalArgumentException::class)
    suspend fun editGroup(group: Group)

    @Throws(AuthenticationException::class, IllegalArgumentException::class)
    suspend fun deleteGroup(groupId: GroupId)
}

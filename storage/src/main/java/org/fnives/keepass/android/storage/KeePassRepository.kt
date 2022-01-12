package org.fnives.keepass.android.storage

import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.model.Credentials
import org.fnives.keepass.android.storage.model.EntryDetailed
import org.fnives.keepass.android.storage.model.EntryId
import org.fnives.keepass.android.storage.model.Group
import org.fnives.keepass.android.storage.model.GroupId
import org.fnives.keepass.android.storage.model.GroupOrEntry
import org.fnives.keepass.android.storage.model.GroupWithEntries

interface KeePassRepository {

    @Throws(AuthenticationException::class)
    suspend fun authenticate(credentials: Credentials)

    @Throws(AuthenticationException::class)
    suspend fun getGroup(groupId: GroupId) : GroupWithEntries

    @Throws(AuthenticationException::class)
    suspend fun addGroup(group: Group)

    @Throws(AuthenticationException::class)
    suspend fun editGroup(groupId: GroupId, group: Group)

    @Throws(AuthenticationException::class)
    suspend fun getEntry(entryId: EntryId): EntryDetailed

    @Throws(AuthenticationException::class)
    suspend fun addEntry(entry: EntryDetailed, groupId: GroupId)

    @Throws(AuthenticationException::class)
    suspend fun editEntry(entryId: EntryId, entry: EntryDetailed)

    @Throws(AuthenticationException::class)
    suspend fun search(name: String): List<GroupOrEntry>

    @Throws(AuthenticationException::class)
    suspend fun searchByUrl(name: String): List<GroupOrEntry>

    suspend fun disconnect()

    companion object {

        fun getInstance(): KeePassRepository = TODO()
    }
}


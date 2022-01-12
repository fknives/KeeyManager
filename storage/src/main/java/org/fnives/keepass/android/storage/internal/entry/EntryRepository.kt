package org.fnives.keepass.android.storage.internal.entry

import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.model.EntryDetailed
import org.fnives.keepass.android.storage.model.EntryId
import org.fnives.keepass.android.storage.model.GroupId

internal interface EntryRepository {

    @Throws(AuthenticationException::class)
    suspend fun getEntry(entryId: EntryId): EntryDetailed

    @Throws(AuthenticationException::class)
    suspend fun addEntry(entry: EntryDetailed, groupId: GroupId)

    @Throws(AuthenticationException::class)
    suspend fun editEntry(entryId: EntryId, entry: EntryDetailed)

    @Throws(AuthenticationException::class)
    suspend fun deleteEntry(entryId: EntryId)
}
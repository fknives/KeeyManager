package org.fnives.keepass.android.storage.internal.entry

import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.model.EntryDetailed
import org.fnives.keepass.android.storage.model.EntryId
import org.fnives.keepass.android.storage.model.GroupId

internal interface EntryRepository {

    @Throws(AuthenticationException::class)
    suspend fun getEntry(entryId: EntryId): EntryDetailed?

    @Throws(AuthenticationException::class, IllegalArgumentException::class)
    suspend fun addEntry(entry: EntryDetailed, parentId: GroupId): EntryId

    @Throws(AuthenticationException::class, IllegalArgumentException::class)
    suspend fun editEntry(entry: EntryDetailed)

    @Throws(AuthenticationException::class)
    suspend fun deleteEntry(entryId: EntryId)
}
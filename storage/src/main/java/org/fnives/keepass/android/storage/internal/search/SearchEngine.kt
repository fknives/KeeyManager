package org.fnives.keepass.android.storage.internal.search

import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.model.Entry
import org.fnives.keepass.android.storage.model.GroupId
import org.fnives.keepass.android.storage.model.GroupOrEntry

internal interface SearchEngine {

    @Throws(AuthenticationException::class)
    suspend fun search(name: String, scope: GroupId?): List<GroupOrEntry>

    @Throws(AuthenticationException::class)
    suspend fun searchByUrl(name: String, scope: GroupId?): List<Entry>
}
package org.fnives.keepass.android.storage.internal.search

import org.fnives.keepass.android.storage.model.Entry
import org.fnives.keepass.android.storage.model.GroupId
import org.fnives.keepass.android.storage.model.GroupOrEntry

class ActualSearchEngine : SearchEngine {
    override suspend fun search(name: String, scope: GroupId?): List<GroupOrEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun searchByUrl(name: String, scope: GroupId?): List<Entry> {
        TODO("Not yet implemented")
    }
}
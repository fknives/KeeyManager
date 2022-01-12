package org.fnives.keepass.android.storage.internal

import org.fnives.keepass.android.storage.KeePassRepository
import org.fnives.keepass.android.storage.internal.authentication.ActualDatabaseAuthenticationEngine
import org.fnives.keepass.android.storage.internal.authentication.DatabaseAuthenticationEngine
import org.fnives.keepass.android.storage.internal.database.ActualDatabaseHolder
import org.fnives.keepass.android.storage.internal.entry.ActualEntryRepository
import org.fnives.keepass.android.storage.internal.entry.EntryRepository
import org.fnives.keepass.android.storage.internal.group.ActualGroupRepository
import org.fnives.keepass.android.storage.internal.group.GroupRepository
import org.fnives.keepass.android.storage.internal.search.ActualSearchEngine
import org.fnives.keepass.android.storage.internal.search.SearchEngine
import org.fnives.keepass.android.storage.internal.util.DispatcherHolder

internal class ActualKeePassRepository(
    private val groupRepository: GroupRepository,
    private val entryRepository: EntryRepository,
    private val databaseAuthenticationEngine: DatabaseAuthenticationEngine,
    private val searchEngine: SearchEngine
) : KeePassRepository,
    GroupRepository by groupRepository,
    EntryRepository by entryRepository,
    DatabaseAuthenticationEngine by databaseAuthenticationEngine,
    SearchEngine by searchEngine {

    companion object {

        fun getInstance(
            dispatcherHolder: DispatcherHolder = DispatcherHolder(),
            actualKPDatabaseHolder: ActualDatabaseHolder = ActualDatabaseHolder()
        ): ActualKeePassRepository {
            return ActualKeePassRepository(
                groupRepository = ActualGroupRepository(
                    databaseHolder = actualKPDatabaseHolder
                ),
                entryRepository = ActualEntryRepository(
                    databaseHolder = actualKPDatabaseHolder
                ),
                databaseAuthenticationEngine = ActualDatabaseAuthenticationEngine(
                    databaseHolder = actualKPDatabaseHolder,
                    dispatcherHolder = dispatcherHolder
                ),
                searchEngine = ActualSearchEngine()
            )
        }
    }
}
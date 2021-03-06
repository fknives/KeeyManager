package org.fnives.keepass.android.storage

import org.fnives.keepass.android.storage.KeePassRepository.Companion.getInstance
import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.internal.ActualKeePassRepository
import org.fnives.keepass.android.storage.model.Credentials
import org.fnives.keepass.android.storage.model.Entry
import org.fnives.keepass.android.storage.model.EntryDetailed
import org.fnives.keepass.android.storage.model.EntryId
import org.fnives.keepass.android.storage.model.Group
import org.fnives.keepass.android.storage.model.GroupId
import org.fnives.keepass.android.storage.model.GroupOrEntry
import org.fnives.keepass.android.storage.model.GroupWithEntries

/**
 * Access Point to the Encrypted Database
 *
 * Get instance via [getInstance].
 * After [authenticate] it will keep a reference to the encrypted database and all getters and
 * modifiers will modify that database.
 *
 * With [disconnect] you can disconnect from the database and re-authenticate.
 *
 * [AuthenticationException] is thrown if the Database is not Authenticated, or Authentication failed.
 */
interface KeePassRepository {

    /**
     * Connect to the Database, all getters and modifiers will work on the authenticated database.
     */
    @Throws(AuthenticationException::class)
    suspend fun authenticate(credentials: Credentials)

    /**
     * Returns the Group with it's basic Entry models to show to the user.
     *
     * For root group refer [GroupId.ROOT_ID]
     */
    @Throws(AuthenticationException::class)
    suspend fun getGroup(groupId: GroupId): GroupWithEntries?

    /**
     * Creates a new group in the database, given [GroupId] is ignored
     */
    @Throws(AuthenticationException::class, IllegalArgumentException::class)
    suspend fun addGroup(group: Group, parentId: GroupId): GroupId

    @Throws(AuthenticationException::class, IllegalArgumentException::class)
    suspend fun editGroup(group: Group)

    @Throws(AuthenticationException::class, IllegalArgumentException::class)
    suspend fun deleteGroup(groupId: GroupId)

    /**
     * Returns the detailed Entry Model with all it's modifiable fields
     */
    @Throws(AuthenticationException::class)
    suspend fun getEntry(entryId: EntryId): EntryDetailed?

    /**
     * Creates a new entry in the database, given [EntryId] and [EntryDetailed.lastModified] is ignored
     */
    @Throws(AuthenticationException::class, IllegalArgumentException::class)
    suspend fun addEntry(entry: EntryDetailed, parentId: GroupId): EntryId

    @Throws(AuthenticationException::class, IllegalArgumentException::class)
    suspend fun editEntry(entry: EntryDetailed)

    @Throws(AuthenticationException::class)
    suspend fun deleteEntry(entryId: EntryId)

    /**
     * Search Groups and entries by their name
     */
    @Throws(AuthenticationException::class)
    suspend fun search(name: String, scope: GroupId = GroupId.ROOT_ID): List<GroupOrEntry>

    /**
     * Search entries by their url
     */
    @Throws(AuthenticationException::class)
    suspend fun searchByUsername(username: String, scope: GroupId = GroupId.ROOT_ID): List<Entry>

    /**
     * Disconnect from the
     */
    suspend fun disconnect()

    companion object {

        fun getInstance(): KeePassRepository =
            ActualKeePassRepository.getInstance()
    }
}

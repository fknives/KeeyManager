package org.fnives.keepass.android.storage.model

import java.util.Date

/**
 * Detailed of an entry, intended to show the user it's content and be able to make changes.
 */
data class EntryDetailed(
    val id: EntryId = EntryId.GENERATE_ID,
    val entryName: String,
    val userName: String,
    val password: String,
    val url: String,
    val notes: String,
    val icon: KIcon = KIcon.Key,
    val lastModified: Date = Date()
)
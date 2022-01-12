package org.fnives.keepass.android.storage.model

import java.util.Date

class EntryDetailed(
    val id: EntryId,
    val entryName: String,
    val userName: String,
    val password: String,
    val url: String,
    val notes: String,
    val lastModified: Date,
    val icon: KIcon
)
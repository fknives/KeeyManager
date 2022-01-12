package org.fnives.keepass.android.storage.model

data class Entry(
    val id: EntryId,
    val entryName: String
) : GroupOrEntry()
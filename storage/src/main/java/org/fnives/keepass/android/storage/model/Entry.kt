package org.fnives.keepass.android.storage.model

/**
 * Basic representation of an entry, intended to show the user for selection
 */
data class Entry(
    val id: EntryId,
    val entryName: String,
    val userName: String,
    val icon: KIcon
) : GroupOrEntry()

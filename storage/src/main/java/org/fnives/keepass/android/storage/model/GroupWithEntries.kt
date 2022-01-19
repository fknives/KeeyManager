package org.fnives.keepass.android.storage.model

/**
 * Detailed of an group, intended to show the user the group and it's content
 */
data class GroupWithEntries(
    val group: Group,
    val entries: List<GroupOrEntry>
)

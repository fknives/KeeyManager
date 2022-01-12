package org.fnives.keepass.android.storage.model

import java.util.Date

/**
 * Basic representation of an Group, intended to show the user for selection
 */
data class Group(
    val id: GroupId = GroupId.ROOT_ID,
    val groupName: String,
    val icon: KIcon
) : GroupOrEntry()
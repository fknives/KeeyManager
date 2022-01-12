package org.fnives.keepass.android.storage.model

import java.util.Date

/**
 * Basic representation of an Group, intended to show the user for selection
 */
class Group(
    val id: GroupId,
    val lastModified: Date,
    val groupName: String,
    val icon: KIcon
) : GroupOrEntry()
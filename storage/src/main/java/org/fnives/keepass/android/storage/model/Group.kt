package org.fnives.keepass.android.storage.model

import java.util.*

class Group(
    val id: GroupId,
    val lastModified: Date,
    val groupName: String,
    val icon: KIcon
): GroupOrEntry()
package org.fnives.keepass.android.storage.model

/**
 * Basic representation of an Group, intended to show the user for selection
 */
data class Group(
    val id: GroupId = GroupId.GENERATE_ID,
    val groupName: String,
    val entryOrGroupCount: Int = 0,
    val icon: KIcon = KIcon.Folder
) : GroupOrEntry() {
    val isRoot: Boolean = id === GroupId.ROOT_ID
}
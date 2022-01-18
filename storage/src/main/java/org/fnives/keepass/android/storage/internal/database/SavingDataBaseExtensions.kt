package org.fnives.keepass.android.storage.internal.database

import org.fnives.keepass.android.storage.model.GroupId

internal fun SavingDataBase.getDomById(groupId: GroupId) =
    if (groupId === GroupId.ROOT_ID) {
        rootGroup
    } else {
        findGroup(groupId.uuid)
    }
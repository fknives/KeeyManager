package org.fnives.keepass.android.storage.internal

import org.fnives.keepass.android.storage.exception.AuthenticationException
import org.fnives.keepass.android.storage.internal.database.DatabaseHolder
import org.fnives.keepass.android.storage.model.KIcon
import org.linguafranca.pwdb.kdbx.dom.DomIconWrapper

internal class IconConverter(private val databaseHolder: DatabaseHolder) {

    private val values by lazy { KIcon.values() }

    @Throws(AuthenticationException::class)
    fun convert(icon: KIcon): DomIconWrapper = databaseHolder.database.newIcon(icon.ordinal)

    fun convert(dom: DomIconWrapper): KIcon = values.getOrNull(dom.index) ?: KIcon.UNKNOWN
}

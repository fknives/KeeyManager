package org.fnives.keepass.android.storage.internal.search

import org.fnives.keepass.android.storage.internal.IconConverter
import org.fnives.keepass.android.storage.internal.database.DatabaseHolder
import org.fnives.keepass.android.storage.internal.database.getDomById
import org.fnives.keepass.android.storage.internal.group.GroupConverter
import org.fnives.keepass.android.storage.model.Entry
import org.fnives.keepass.android.storage.model.GroupId
import org.fnives.keepass.android.storage.model.GroupOrEntry
import org.linguafranca.pwdb.Entry as DBEntry
import org.linguafranca.pwdb.kdbx.dom.DomGroupWrapper

internal class ActualSearchEngine(
    private val databaseHolder: DatabaseHolder,
    private val groupConverter: GroupConverter = GroupConverter(IconConverter(databaseHolder))
) : SearchEngine {

    private val database get() = databaseHolder.database

    override suspend fun search(name: String, scope: GroupId): List<GroupOrEntry> {
        val dom = database.getDomById(scope) ?: return emptyList()
        val foundGroups = dom.findGroups(name, true).map(groupConverter::convert)
        val foundEntries = dom.findEntries(NameMatcher(name), true).map(groupConverter::convert)

        return foundGroups.plus(foundEntries)
    }

    override suspend fun searchByUsername(username: String, scope: GroupId): List<Entry> =
        database.getDomById(scope)
            ?.findEntries(UsernameMatcher(username), true)
            .orEmpty()
            .map(groupConverter::convert)

    private class UsernameMatcher(private val username: String) : DBEntry.Matcher {
        override fun matches(entry: DBEntry<*, *, *, *>): Boolean = entry.username.containsMatch(username)
    }

    private class NameMatcher(private val name: String) : DBEntry.Matcher {
        override fun matches(entry: DBEntry<*, *, *, *>): Boolean = entry.title.containsMatch(name)
    }

    companion object {
        fun DomGroupWrapper.findGroups(name: String, recursive: Boolean): List<DomGroupWrapper> {
            val elements = findMatchingGroups(name).toMutableList()
            if (!recursive) return elements
            groups.forEach {
                elements.addAll(it.findGroups(name, recursive))
            }
            return elements
        }

        private fun DomGroupWrapper.findMatchingGroups(name: String): List<DomGroupWrapper> =
            groups.filter { it.name.containsMatch(name) }

        private fun String.containsMatch(text: String) = lowercase().contains(text.lowercase())
    }
}

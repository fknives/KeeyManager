package org.fnives.keepass.android.storage

import java.io.InputStream
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.linguafranca.pwdb.Entry
import org.linguafranca.pwdb.Entry.STANDARD_PROPERTY_NAME_PASSWORD
import org.linguafranca.pwdb.Entry.STANDARD_PROPERTY_NAME_USER_NAME
import org.linguafranca.pwdb.Group
import org.linguafranca.pwdb.Visitor
import org.linguafranca.pwdb.kdbx.KdbxCreds
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper

class OpeningDatabaseLibTest {

    @Test
    fun printDatabase() {
        val inputStream: InputStream = javaClass.classLoader.getResourceAsStream("test1.kdbx")
        val databaseAsString = DatabaseAsString(inputStream)
        val expected = """
/Root/
/Root/blabla(alma, banan)
        """.trimIndent()
        val result = databaseAsString.open("test1")

        Assertions.assertEquals(expected, result)
    }

    class DatabaseAsString(private val database: InputStream) {

        fun open(key: String): String {
            val credentials = KdbxCreds(key.toByteArray())
            val database = DomDatabaseWrapper.load(credentials, database)
            val stringVisitor = StringVisitor()
            database.visit(stringVisitor)
            return stringVisitor.getResultString()
        }
    }

    class StringVisitor : Visitor.Default() {
        private val elements = mutableListOf<String>()

        fun getResultString() = elements.joinToString("\n")

        override fun startVisit(group: Group<*, *, *, *>?) {
            if (group?.isRootGroup == true) {
                elements.clear()
            }
            elements.add(group.toString())
        }

        override fun visit(entry: Entry<*, *, *, *>?) {
            val userName = entry?.getProperty(STANDARD_PROPERTY_NAME_USER_NAME)
            val password = entry?.getProperty(STANDARD_PROPERTY_NAME_PASSWORD)
            elements.add("$entry($userName, $password)")
        }
    }
}

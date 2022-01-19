package org.fnives.keepass.android.storage.internal.database

import java.io.OutputStream
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import org.fnives.keepass.android.storage.testutil.TestDispatcherHolder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.linguafranca.pwdb.Credentials
import org.linguafranca.pwdb.Database
import org.linguafranca.pwdb.Entry
import org.linguafranca.pwdb.Visitor
import org.linguafranca.pwdb.kdbx.KdbxCreds
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper
import org.linguafranca.pwdb.kdbx.dom.DomEntryWrapper
import org.linguafranca.pwdb.kdbx.dom.DomGroupWrapper
import org.linguafranca.pwdb.kdbx.dom.DomIconWrapper
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
internal class SavingDomDatabaseWrapperTest {

    private lateinit var sut: SavingDataBase
    private lateinit var mockCredentials: KdbxCreds
    private lateinit var mockDatabase: Database<DomDatabaseWrapper, DomGroupWrapper, DomEntryWrapper, DomIconWrapper>
    private lateinit var mockOutputStreamFactory: () -> OutputStream
    private lateinit var testDispatcherHolder: TestDispatcherHolder

    @BeforeEach
    fun setUp() {
        mockCredentials = mock()
        mockDatabase = mock()
        mockOutputStreamFactory = mock()
        testDispatcherHolder = TestDispatcherHolder(startPaused = true)

        sut = SavingDomDatabaseWrapper(
            credentials = mockCredentials,
            delegate = mockDatabase,
            outputStreamFactory = mockOutputStreamFactory,
            dispatcher = testDispatcherHolder.dispatcherHolder.single
        )
    }

    @Test
    fun verifySaving() {
        val coroutineScope = TestCoroutineScope()
        coroutineScope.launch { sut.save() }
        coroutineScope.advanceUntilIdle()
        verifyZeroInteractions(mockDatabase)

        val mockOutputStream = mock<OutputStream>()
        whenever(mockOutputStreamFactory.invoke()).doReturn(mockOutputStream)
        testDispatcherHolder.single.runCurrent()
        verify(mockDatabase).save(mockCredentials, mockOutputStream)
    }

    @Test
    fun verifyCallsAreDelegated() {
        verifyZeroInteractions(mockDatabase)
        sut.newGroup()
        verify(mockDatabase).newGroup()
        verifyNoMoreInteractions(mockDatabase)

        sut.newGroup("alma")
        verify(mockDatabase).newGroup("alma")
        verifyNoMoreInteractions(mockDatabase)

        val mockGroup = mock<DomGroupWrapper>()
        sut.newGroup(mockGroup)
        verify(mockDatabase).newGroup(mockGroup)
        verifyNoMoreInteractions(mockDatabase)

        sut.newIcon()
        verify(mockDatabase).newIcon()
        verifyNoMoreInteractions(mockDatabase)

        sut.newIcon(1)
        verify(mockDatabase).newIcon(1)
        verifyNoMoreInteractions(mockDatabase)

        val groupId = UUID(1, 2)
        sut.findGroup(groupId)
        verify(mockDatabase).findGroup(groupId)
        verifyNoMoreInteractions(mockDatabase)

        sut.deleteGroup(groupId)
        verify(mockDatabase).deleteGroup(groupId)
        verifyNoMoreInteractions(mockDatabase)

        sut.isRecycleBinEnabled
        verify(mockDatabase).isRecycleBinEnabled
        verifyNoMoreInteractions(mockDatabase)

        sut.enableRecycleBin(false)
        verify(mockDatabase).enableRecycleBin(false)
        verifyNoMoreInteractions(mockDatabase)

        sut.recycleBin
        verify(mockDatabase).recycleBin
        verifyNoMoreInteractions(mockDatabase)

        sut.emptyRecycleBin()
        verify(mockDatabase).emptyRecycleBin()
        verifyNoMoreInteractions(mockDatabase)

        sut.findEntries("banan")
        verify(mockDatabase).findEntries("banan")
        verifyNoMoreInteractions(mockDatabase)

        val mockMatcher = mock<Entry.Matcher>()
        sut.findEntries(mockMatcher)
        verify(mockDatabase).findEntries(mockMatcher)
        verifyNoMoreInteractions(mockDatabase)

        sut.description = "desc"
        verify(mockDatabase).description = "desc"
        verifyNoMoreInteractions(mockDatabase)

        sut.shouldProtect("citrom")
        verify(mockDatabase).shouldProtect("citrom")
        verifyNoMoreInteractions(mockDatabase)

        sut.supportsNonStandardPropertyNames()
        verify(mockDatabase).supportsNonStandardPropertyNames()
        verifyNoMoreInteractions(mockDatabase)

        sut.supportsBinaryProperties()
        verify(mockDatabase).supportsBinaryProperties()
        verifyNoMoreInteractions(mockDatabase)

        sut.supportsRecycleBin()
        verify(mockDatabase).supportsRecycleBin()
        verifyNoMoreInteractions(mockDatabase)

        sut.description
        verify(mockDatabase).description
        verifyNoMoreInteractions(mockDatabase)

        sut.newEntry()
        verify(mockDatabase).newEntry()
        verifyNoMoreInteractions(mockDatabase)

        sut.newEntry("almafa")
        verify(mockDatabase).newEntry("almafa")
        verifyNoMoreInteractions(mockDatabase)

        val mockEntry = mock<DomEntryWrapper>()
        sut.newEntry(mockEntry)
        verify(mockDatabase).newEntry(mockEntry)
        verifyNoMoreInteractions(mockDatabase)

        sut.isDirty
        verify(mockDatabase).isDirty
        verifyNoMoreInteractions(mockDatabase)

        sut.name
        verify(mockDatabase).name
        verifyNoMoreInteractions(mockDatabase)

        sut.findEntry(UUID(35, 12))
        verify(mockDatabase).findEntry(UUID(35, 12))
        verifyNoMoreInteractions(mockDatabase)

        sut.rootGroup
        verify(mockDatabase).rootGroup
        verifyNoMoreInteractions(mockDatabase)

        sut.name = "UUID(35,12)"
        verify(mockDatabase).name = "UUID(35,12)"
        verifyNoMoreInteractions(mockDatabase)

        val mockVisitor = mock<Visitor>()
        sut.visit(mockVisitor)
        verify(mockDatabase).visit(mockVisitor)
        verifyNoMoreInteractions(mockDatabase)

        sut.visit(mockGroup, mockVisitor)
        verify(mockDatabase).visit(mockGroup, mockVisitor)
        verifyNoMoreInteractions(mockDatabase)

        sut.deleteEntry(UUID(4, 0))
        verify(mockDatabase).deleteEntry(UUID(4, 0))
        verifyNoMoreInteractions(mockDatabase)

        val mockCredentials = mock<Credentials>()
        val mockOutputStream = mock<OutputStream>()
        sut.save(mockCredentials, mockOutputStream)
        verify(mockDatabase).save(mockCredentials, mockOutputStream)
        verifyNoMoreInteractions(mockDatabase)
    }
}

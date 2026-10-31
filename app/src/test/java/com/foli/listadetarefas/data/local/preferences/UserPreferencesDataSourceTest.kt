package com.foli.listadetarefas.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.foli.listadetarefas.data.repository.PreferencesRepositoryImpl
import com.foli.listadetarefas.domain.model.SortOrder
import com.foli.listadetarefas.domain.model.TaskTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Persistência real da ordenação (US-07 AC 4 / correção M5): exercita
 * `UserPreferencesDataSource` e `PreferencesRepositoryImpl` contra um
 * `DataStore<Preferences>` de verdade, gravando em um arquivo temporário — não
 * apenas o fake em memória da suíte de ViewModel.
 *
 * Não há dependência nova: o teste usa a mesma API de produção
 * (`PreferenceDataStoreFactory`), apenas com outro arquivo de destino.
 */
class UserPreferencesDataSourceTest {

    @get:Rule
    val tempFolder: TemporaryFolder = TemporaryFolder()

    private val preferencesFile: File by lazy {
        File(tempFolder.root, "user_preferences.preferences_pb")
    }

    private lateinit var scope: CoroutineScope
    private lateinit var dataStore: DataStore<Preferences>

    @Before
    fun setUp() {
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        dataStore = PreferenceDataStoreFactory.create(scope = scope) { preferencesFile }
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun dataSource(store: DataStore<Preferences> = dataStore) =
        UserPreferencesDataSource(store, Dispatchers.IO)

    @Test
    fun emptyStore_fallsBackToActiveTabAndCreationMostRecentFirst() = runBlocking {
        val preferences = dataSource().preferences.first()

        assertEquals(TaskTab.ACTIVE, preferences.selectedTab)
        assertEquals(SortOrder.CREATED_DESC, preferences.sortOrder)
    }

    @Test
    fun setSortOrder_writesTheEnumNameAndReadsItBack() = runBlocking {
        val source = dataSource()

        source.setSortOrder(SortOrder.DEADLINE_DESC)

        assertEquals(SortOrder.DEADLINE_DESC, source.preferences.first().sortOrder)
        assertTrue(
            "O arquivo do DataStore deveria existir após a gravação.",
            preferencesFile.exists() && preferencesFile.length() > 0L,
        )
    }

    @Test
    fun unknownStoredValue_fallsBackToDefaultWithoutThrowing() = runBlocking {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("sort_order")] = "NOT_A_SORT_ORDER"
        }

        assertEquals(SortOrder.CREATED_DESC, dataSource().preferences.first().sortOrder)
    }

    @Test
    fun chosenSortOrder_survivesAReopenOfTheStore() = runBlocking {
        dataSource().setSortOrder(SortOrder.TITLE_DESC)

        // Fecha o "app": encerra o escopo (e o actor) do primeiro DataStore.
        scope.cancel()
        scope.coroutineContext[Job]?.join()

        // Reabre sobre o mesmo arquivo, como um novo processo faria.
        val reopenedScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        try {
            val reopenedStore = PreferenceDataStoreFactory.create(scope = reopenedScope) {
                preferencesFile
            }
            val reopened = dataSource(reopenedStore).preferences.first()

            assertEquals(SortOrder.TITLE_DESC, reopened.sortOrder)
        } finally {
            reopenedScope.cancel()
        }
    }

    @Test
    fun repository_delegatesWritesAndReadsToTheRealDataStore() = runBlocking {
        val repository = PreferencesRepositoryImpl(dataSource())

        repository.setSortOrder(SortOrder.TITLE_ASC)

        assertEquals(SortOrder.TITLE_ASC, repository.observePreferences().first().sortOrder)
    }
}

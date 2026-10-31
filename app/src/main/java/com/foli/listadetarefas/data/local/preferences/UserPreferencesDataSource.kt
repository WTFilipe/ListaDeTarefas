package com.foli.listadetarefas.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.foli.listadetarefas.core.di.IoDispatcher
import com.foli.listadetarefas.data.mapper.PreferencesMapper
import com.foli.listadetarefas.domain.model.SortOrder
import com.foli.listadetarefas.domain.model.TaskTab
import com.foli.listadetarefas.domain.model.UserPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

/**
 * Fonte de preferências em DataStore. Leituras são reativas e tolerantes a
 * corrupção (caem nos defaults); escritas rodam em [IoDispatcher]
 * (ARCHITECTURE.md §9.2).
 */
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    val preferences: Flow<UserPreferences> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs ->
            PreferencesMapper.toDomain(
                selectedTabName = prefs[KEY_SELECTED_TAB],
                sortOrderName = prefs[KEY_SORT_ORDER],
            )
        }

    suspend fun setSelectedTab(tab: TaskTab) {
        withContext(ioDispatcher) {
            dataStore.edit { prefs ->
                prefs[KEY_SELECTED_TAB] = tab.name
            }
        }
    }

    suspend fun setSortOrder(order: SortOrder) {
        withContext(ioDispatcher) {
            dataStore.edit { prefs ->
                prefs[KEY_SORT_ORDER] = order.name
            }
        }
    }

    companion object {
        val KEY_SELECTED_TAB: Preferences.Key<String> = stringPreferencesKey("selected_tab")
        val KEY_SORT_ORDER: Preferences.Key<String> = stringPreferencesKey("sort_order")
    }
}

package com.djoudini.player.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext private val context: Context) {
    
    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val EXP_DATE = stringPreferencesKey("exp_date")
        val SERVER_URL = stringPreferencesKey("server_url")
        val USERNAME = stringPreferencesKey("username")
        val PASSWORD = stringPreferencesKey("password")
        val LIVE_CATEGORIES = stringSetPreferencesKey("live_categories")
        val VOD_CATEGORIES = stringSetPreferencesKey("vod_categories")
        val SERIES_CATEGORIES = stringSetPreferencesKey("series_categories")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_LOGGED_IN] ?: false }
    val expDate: Flow<String> = context.dataStore.data.map { it[Keys.EXP_DATE] ?: "Unknown" }

    suspend fun getAccountInfo() = context.dataStore.data.map {
        mapOf(
            "server" to it[Keys.SERVER_URL],
            "user" to it[Keys.USERNAME],
            "pass" to it[Keys.PASSWORD]
        )
    }.first()

    suspend fun getSelectedLiveCategories() = context.dataStore.data.map { it[Keys.LIVE_CATEGORIES] ?: emptySet() }.first()
    
    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { it[Keys.IS_LOGGED_IN] = loggedIn }
    }

    suspend fun saveAccountInfo(server: String, user: String, pass: String, exp: String) {
        context.dataStore.edit {
            it[Keys.SERVER_URL] = server
            it[Keys.USERNAME] = user
            it[Keys.PASSWORD] = pass
            it[Keys.EXP_DATE] = exp
        }
    }

    suspend fun saveCategorySelections(live: List<String>, vod: List<String>, series: List<String>) {
        context.dataStore.edit {
            it[Keys.LIVE_CATEGORIES] = live.toSet()
            it[Keys.VOD_CATEGORIES] = vod.toSet()
            it[Keys.SERIES_CATEGORIES] = series.toSet()
        }
    }

    suspend fun logout() {
        context.dataStore.edit { it.clear() }
    }
}

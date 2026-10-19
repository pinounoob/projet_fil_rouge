package com.exemple.reveil.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("parametres")

class PreferenceManager(private val context: Context) {
    val THEME_CHOICE = stringPreferencesKey("theme_choice") // "CLAIR", "SOMBRE", "SYSTEME"
    val USER_ID = stringPreferencesKey("user_id")
    val STAY_CONNECTED = booleanPreferencesKey("stay_connected")
    val PSEUDO = stringPreferencesKey("pseudo")
    val DEFAULT_BET = intPreferencesKey("default_bet")
    val RINGTONE_MODE = stringPreferencesKey("ringtone_mode") // "SONNERIE" ou "VIBRATION"
    // Autres clés à ajouter ici plus tard


    val themeFlow: Flow<String> = context.dataStore.data.map { it[THEME_CHOICE] ?: "SYSTEME" }
    val stayConnectedFlow: Flow<Boolean> = context.dataStore.data.map { it[STAY_CONNECTED] ?: false }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { it[THEME_CHOICE] = theme }
    }

    suspend fun saveSession(uid: String, pseudo: String, stayConnected: Boolean) {
        context.dataStore.edit {
            it[USER_ID] = uid
            it[PSEUDO] = pseudo
            it[STAY_CONNECTED] = stayConnected
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit {
            it.remove(USER_ID)
            it.remove(STAY_CONNECTED)
            it.remove(PSEUDO)
        }
    }
}
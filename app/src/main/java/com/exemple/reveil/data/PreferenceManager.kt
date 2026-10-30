package com.exemple.reveil.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class PreferenceManager(private val context: Context) {

    companion object {
        val THEME_CHOICE = stringPreferencesKey("theme_choice") // "CLAIR", "SOMBRE", "SYSTEME"
        val USER_ID = stringPreferencesKey("user_id")
        val STAY_CONNECTED = booleanPreferencesKey("stay_connected")
        val PSEUDO = stringPreferencesKey("pseudo")
        val DEFAULT_BET = intPreferencesKey("default_bet")
        val RINGTONE_MODE = stringPreferencesKey("ringtone_mode") // "SONNERIE" ou "VIBRATION"

        val SELECTED_GROUP = stringPreferencesKey("selected_group") //selection du groupe
        val SOLDE = intPreferencesKey("solde")
        val RINGTONE_URI = stringPreferencesKey("ringtone_uri") // fichier audio choisi
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { it[THEME_CHOICE] ?: "SYSTEME" }
    val stayConnectedFlow: Flow<Boolean> = context.dataStore.data.map { it[STAY_CONNECTED] ?: false }
    val pseudoFlow: Flow<String> = context.dataStore.data.map { it[PSEUDO] ?: "" }
    val defaultBetFlow: Flow<Int> = context.dataStore.data.map { it[DEFAULT_BET] ?: 10 }
    val ringtoneModeFlow: Flow<String> = context.dataStore.data.map { it[RINGTONE_MODE] ?: "SONNERIE" }

    val selectedGroupFlow: Flow<String?> = context.dataStore.data.map { it[SELECTED_GROUP] }
    val soldeFlow: Flow<Int> = context.dataStore.data.map { it[SOLDE] ?: 0 }
    val ringtoneUriFlow: Flow<String?> = context.dataStore.data.map { it[RINGTONE_URI] }
    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { it[THEME_CHOICE] = theme }
    }

    suspend fun savePseudo(pseudo: String) {
        context.dataStore.edit { it[PSEUDO] = pseudo }
    }

    suspend fun saveDefaultBet(bet: Int) {
        context.dataStore.edit { it[DEFAULT_BET] = bet }
    }

    suspend fun saveRingtoneMode(mode: String) {
        context.dataStore.edit { it[RINGTONE_MODE] = mode }
    }

    suspend fun saveSession(uid: String, pseudo: String, stayConnected: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = uid
            preferences[PSEUDO] = pseudo
            preferences[STAY_CONNECTED] = stayConnected
        }
    }
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID)
            preferences.remove(STAY_CONNECTED)
            // Note : on ne supprime pas le thème ou la sonnerie pour que l'utilisateur
            // garde ses préférences de confort même s'il se déconnecte.
        }
    }

    suspend fun saveSelectedGroup(groupeId: String) {
        context.dataStore.edit { it[SELECTED_GROUP] = groupeId }
    }

    suspend fun addSolde(montant: Int) {
        context.dataStore.edit { preferences ->
            val soldeActuel = preferences[SOLDE] ?: 0
            preferences[SOLDE] = soldeActuel + montant
        }
    }

    suspend fun saveRingtoneUri(uri: String) {
        context.dataStore.edit { it[RINGTONE_URI] = uri }
    }

}
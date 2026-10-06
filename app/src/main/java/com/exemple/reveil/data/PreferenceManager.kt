package com.exemple.reveil.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("parametres")

class PreferencesManager(private val context: Context) {
    private val CLE_EST_SOMBRE = booleanPreferencesKey("est_sombre")

    // Lire la préférence
    val estSombre: Flow<Boolean> = context.dataStore.data.map {
                    preferences -> preferences[CLE_EST_SOMBRE] ?: false
                }

    // Écrire la préférence
    suspend fun definirThemeSombre(sombre: Boolean) {
        context.dataStore.edit { preferences -> preferences[CLE_EST_SOMBRE] = sombre }
    }
}
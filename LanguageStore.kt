package com.hanif.kajlagbe

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("settings")

object LanguageStore {

    private val LANGUAGE_KEY = booleanPreferencesKey("isEnglish")

    fun getLanguage(context: Context): Flow<Boolean> =
        context.dataStore.data.map {
            it[LANGUAGE_KEY] ?: true
        }

    suspend fun saveLanguage(context: Context, isEnglish: Boolean) {
        context.dataStore.edit {
            it[LANGUAGE_KEY] = isEnglish
        }
    }
}

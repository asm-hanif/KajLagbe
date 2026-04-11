package com.hanif.kajlagbe

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("settings")

object LanguageStore {

    private val LANGUAGE_KEY = stringPreferencesKey("language")

    fun getLanguage(context: Context): Flow<String> =
        context.dataStore.data.map {
            it[LANGUAGE_KEY] ?: "en"
        }

    suspend fun saveLanguage(context: Context, lang: String) {
        context.dataStore.edit {
            it[LANGUAGE_KEY] = lang
        }
    }
}

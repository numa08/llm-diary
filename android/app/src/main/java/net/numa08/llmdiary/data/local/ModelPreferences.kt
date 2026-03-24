package net.numa08.llmdiary.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "model_preferences")

@Singleton
class ModelPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val modelPathKey = stringPreferencesKey("model_path")

    val modelPath: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[modelPathKey]
    }

    suspend fun setModelPath(path: String) {
        context.dataStore.edit { prefs ->
            prefs[modelPathKey] = path
        }
    }

    suspend fun clearModelPath() {
        context.dataStore.edit { prefs ->
            prefs.remove(modelPathKey)
        }
    }
}

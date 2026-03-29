package app.rednote_m25.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val userAvatarKey = stringPreferencesKey("user_avatar_url")
    private val themeModeKey = stringPreferencesKey("theme_mode")

    val userAvatarUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[userAvatarKey] ?: ""
    }

    val themeMode: Flow<AppThemeMode> = context.dataStore.data.map { preferences ->
        val modeString = preferences[themeModeKey] ?: AppThemeMode.SYSTEM.name
        try {
            AppThemeMode.valueOf(modeString)
        } catch (e: IllegalArgumentException) {
            AppThemeMode.SYSTEM
        }
    }

    suspend fun updateUserAvatar(avatarUrl: String) {
        context.dataStore.edit { preferences ->
            preferences[userAvatarKey] = avatarUrl
        }
    }

    suspend fun updateThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[themeModeKey] = mode.name
        }
    }
}
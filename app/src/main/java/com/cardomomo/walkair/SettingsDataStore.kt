import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión del contexto
val Context.dataStore by preferencesDataStore(name = "settings")

object SettingsDataStore {
    // Claves
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val USERNAME = stringPreferencesKey("Usuario")
    val STEP_GOAL = intPreferencesKey("step_goal")
    val CALORIES_GOAL = floatPreferencesKey("calories_goal")

    // Guardar un valor
    suspend fun setDarkMode(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
        }
    }

    suspend fun setUsername(context: Context, name: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME] = name
        }
    }

    suspend fun setStepGoal(context: Context, goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[STEP_GOAL] = goal
        }
    }

    suspend fun setCaloriesGoal(context: Context, goal: Float) {
        context.dataStore.edit { it[CALORIES_GOAL] = goal }
    }

    suspend fun clearAll(context: Context) {
        context.dataStore.edit { it.clear() }
    }

    // Leer los valores
    val darkModeFlow: (Context) -> Flow<Boolean> = { context ->
        context.dataStore.data.map { it[DARK_MODE] ?: false }
    }

    val usernameFlow: (Context) -> Flow<String> = { context ->
        context.dataStore.data.map { it[USERNAME] ?: "Usuario" }
    }

    val stepGoalFlow: (Context) -> Flow<Int> = { context ->
        context.dataStore.data.map { it[STEP_GOAL] ?: 6000 }
    }

    val caloriesGoalFlow: (Context) -> Flow<Float> = { context ->
        context.dataStore.data.map { it[CALORIES_GOAL] ?: 300.0f }
    }
}

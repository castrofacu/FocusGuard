package com.facucastro.focusguard.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.facucastro.focusguard.domain.model.FocusSession
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "sessions")

@Singleton
class SessionDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) : LocalSessionDataSource {

    private val gson = Gson()
    private val sessionsType = object : TypeToken<List<FocusSession>>() {}.type

    companion object {
        val SESSIONS_KEY = stringPreferencesKey("focus_sessions")
    }

    override fun getSessions(): Flow<List<FocusSession>> = context.sessionDataStore.data.map { prefs ->
        val json = prefs[SESSIONS_KEY] ?: return@map emptyList()
        gson.fromJson(json, sessionsType) ?: emptyList()
    }

    suspend fun writeSessions(sessions: List<FocusSession>) {
        val json = gson.toJson(sessions)
        context.sessionDataStore.edit { prefs ->
            prefs[SESSIONS_KEY] = json
        }
    }

    override suspend fun addSession(session: FocusSession) {
        val current = getSessions().first().toMutableList()
        current.add(session)
        writeSessions(current)
    }

    // These methods are temporary no-ops while Room replaces this implementation.
    override suspend fun getPendingSessions(): List<FocusSession> = emptyList()

    // No-op for the same reason: DataStore does not track an isSynced flag per session.
    override suspend fun markAsSynced(id: Long) = Unit
}

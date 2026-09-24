package com.gogart.finflow.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.dataStore by preferencesDataStore(name = "settings")

class SecurityManager(private val context: Context) {

    private val pinKey = stringPreferencesKey("pin_hash")
    private val saltKey = stringPreferencesKey("pin_salt")

    val isPinSet: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[pinKey] != null
    }

    suspend fun setPin(pin: String?) {
        context.dataStore.edit { preferences ->
            if (pin == null) {
                preferences.remove(pinKey)
                preferences.remove(saltKey)
            } else {
                val salt = generateSalt()
                val hash = hashPin(pin, salt)
                preferences[pinKey] = hash
                preferences[saltKey] = salt
            }
        }
    }

    suspend fun validatePin(pin: String, onSuccess: () -> Unit, onError: () -> Unit) {
        context.dataStore.edit { preferences ->
            val storedHash = preferences[pinKey]
            val storedSalt = preferences[saltKey]

            if (storedHash != null && storedSalt != null) {
                val inputHash = hashPin(pin, storedSalt)
                if (inputHash == storedHash) {
                    onSuccess()
                } else {
                    onError()
                }
            } else {
                onError()
            }
        }
    }

    private fun hashPin(pin: String, salt: String): String {
        val bytes = "$pin$salt".toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun generateSalt(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..16)
            .map { allowedChars.random() }
            .joinToString("")
    }
}

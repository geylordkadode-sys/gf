package com.berling.marketplace.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.json.Json
import com.berling.marketplace.data.models.AuthUser
import com.berling.marketplace.data.models.AuthToken

class SecurePreferences(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedSharedPreferences = EncryptedSharedPreferences.create(
        context,
        "berling_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val json = Json { ignoreUnknownKeys = true }

    fun saveUser(user: AuthUser) {
        encryptedSharedPreferences.edit().apply {
            putString("user", json.encodeToString(AuthUser.serializer(), user))
            apply()
        }
    }

    fun getUser(): AuthUser? {
        val userJson = encryptedSharedPreferences.getString("user", null) ?: return null
        return try {
            json.decodeFromString(AuthUser.serializer(), userJson)
        } catch (e: Exception) {
            null
        }
    }

    fun saveToken(token: AuthToken) {
        encryptedSharedPreferences.edit().apply {
            putString("token", json.encodeToString(AuthToken.serializer(), token))
            putLong("tokenExpiry", System.currentTimeMillis() + (token.expiresIn * 1000L))
            apply()
        }
    }

    fun getToken(): AuthToken? {
        val tokenJson = encryptedSharedPreferences.getString("token", null) ?: return null
        return try {
            json.decodeFromString(AuthToken.serializer(), tokenJson)
        } catch (e: Exception) {
            null
        }
    }

    fun isTokenExpired(): Boolean {
        val expiry = encryptedSharedPreferences.getLong("tokenExpiry", 0L)
        return System.currentTimeMillis() > expiry
    }

    fun savePaymentToken(paymentMethod: String, token: String, metadata: String = "") {
        encryptedSharedPreferences.edit().apply {
            putString("payment_token_$paymentMethod", token)
            putString("payment_meta_$paymentMethod", metadata)
            apply()
        }
    }

    fun getPaymentToken(paymentMethod: String): String? {
        return encryptedSharedPreferences.getString("payment_token_$paymentMethod", null)
    }

    fun getPaymentMetadata(paymentMethod: String): String? {
        return encryptedSharedPreferences.getString("payment_meta_$paymentMethod", null)
    }

    fun clearAuth() {
        encryptedSharedPreferences.edit().apply {
            remove("user")
            remove("token")
            remove("tokenExpiry")
            apply()
        }
    }

    fun logout() {
        clearAuth()
    }
}

package com.berling.marketplace.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import java.util.*

/**
 * DeviceAccountManager handles device identification and account limits per device.
 * Ensures only 2 accounts can be created per device.
 */
object DeviceAccountManager {

    private const val MAX_ACCOUNTS_PER_DEVICE = 2
    private const val DEVICE_ID_PREF = "device_id"
    private const val DEVICE_ID_PREF_FILE = "device_prefs"

    /**
     * Get or generate a unique device ID
     */
    fun getDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences(DEVICE_ID_PREF_FILE, Context.MODE_PRIVATE)
        var deviceId = prefs.getString(DEVICE_ID_PREF, null)

        if (deviceId == null) {
            deviceId = generateDeviceId(context)
            prefs.edit().putString(DEVICE_ID_PREF, deviceId).apply()
        }

        return deviceId
    }

    /**
     * Generate a unique device ID based on device hardware
     */
    private fun generateDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        val deviceId = StringBuilder()
        deviceId.append(Build.MANUFACTURER).append("_")
        deviceId.append(Build.MODEL).append("_")
        deviceId.append(androidId)

        return deviceId.toString().hashCode().toString()
    }

    /**
     * Check if device can create more accounts
     */
    fun canCreateAccount(
        context: Context,
        deviceId: String,
        currentAccountCount: Int
    ): Boolean {
        return currentAccountCount < MAX_ACCOUNTS_PER_DEVICE
    }

    /**
     * Get the number of accounts on this device
     */
    fun getAccountCountOnDevice(accountCount: Int): Int {
        return minOf(accountCount, MAX_ACCOUNTS_PER_DEVICE)
    }

    /**
     * Check if device limit is reached
     */
    fun isDeviceLimitReached(accountCount: Int): Boolean {
        return accountCount >= MAX_ACCOUNTS_PER_DEVICE
    }

    /**
     * Get remaining account slots for device
     */
    fun getRemainingAccountSlots(accountCount: Int): Int {
        return maxOf(0, MAX_ACCOUNTS_PER_DEVICE - accountCount)
    }
}

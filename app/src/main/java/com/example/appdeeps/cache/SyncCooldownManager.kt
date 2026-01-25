package com.example.appdeeps.cache

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SYNC COOLDOWN MANAGER
 * Prevents users from syncing too frequently (30-minute cooldown)
 * Saves Firebase costs and prevents server overload
 */
class SyncCooldownManager(private val context: Context) {

    private val sharedPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("sync_cooldown", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_LAST_SCHOOL_SYNC = "last_school_sync_time"
        private const val KEY_LAST_EMERGENCY_SYNC = "last_emergency_sync_time"
        private const val COOLDOWN_MINUTES = 30L  // 30 minutes cooldown
    }

    /**
     * Check if school sync is allowed (30 minutes have passed since last sync)
     */
    suspend fun canSyncSchools(): Boolean {
        return withContext(Dispatchers.IO) {
            val lastSync = sharedPrefs.getLong(KEY_LAST_SCHOOL_SYNC, 0)
            if (lastSync == 0L) return@withContext true  // First time sync

            val currentTime = System.currentTimeMillis()
            val minutesPassed = (currentTime - lastSync) / (1000 * 60)

            minutesPassed >= COOLDOWN_MINUTES
        }
    }

    /**
     * Check if emergency sync is allowed
     */
    suspend fun canSyncEmergency(): Boolean {
        return withContext(Dispatchers.IO) {
            val lastSync = sharedPrefs.getLong(KEY_LAST_EMERGENCY_SYNC, 0)
            if (lastSync == 0L) return@withContext true

            val currentTime = System.currentTimeMillis()
            val minutesPassed = (currentTime - lastSync) / (1000 * 60)

            minutesPassed >= COOLDOWN_MINUTES
        }
    }

    /**
     * Update last school sync time to now
     */
    suspend fun updateSchoolSyncTime() {
        withContext(Dispatchers.IO) {
            sharedPrefs.edit()
                .putLong(KEY_LAST_SCHOOL_SYNC, System.currentTimeMillis())
                .apply()
        }
    }

    /**
     * Update last emergency sync time to now
     */
    suspend fun updateEmergencySyncTime() {
        withContext(Dispatchers.IO) {
            sharedPrefs.edit()
                .putLong(KEY_LAST_EMERGENCY_SYNC, System.currentTimeMillis())
                .apply()
        }
    }

    /**
     * Get minutes remaining until next sync is allowed
     */
    suspend fun getSchoolSyncCooldownMinutes(): Long {
        return withContext(Dispatchers.IO) {
            val lastSync = sharedPrefs.getLong(KEY_LAST_SCHOOL_SYNC, 0)
            if (lastSync == 0L) return@withContext 0L

            val currentTime = System.currentTimeMillis()
            val minutesPassed = (currentTime - lastSync) / (1000 * 60)
            val minutesRemaining = COOLDOWN_MINUTES - minutesPassed

            if (minutesRemaining < 0) 0 else minutesRemaining
        }
    }

    /**
     * Get emergency sync cooldown minutes
     */
    suspend fun getEmergencySyncCooldownMinutes(): Long {
        return withContext(Dispatchers.IO) {
            val lastSync = sharedPrefs.getLong(KEY_LAST_EMERGENCY_SYNC, 0)
            if (lastSync == 0L) return@withContext 0L

            val currentTime = System.currentTimeMillis()
            val minutesPassed = (currentTime - lastSync) / (1000 * 60)
            val minutesRemaining = COOLDOWN_MINUTES - minutesPassed

            if (minutesRemaining < 0) 0 else minutesRemaining
        }
    }

    /**
     * Force reset cooldown (for testing or admin use)
     */
    fun resetCooldown() {
        sharedPrefs.edit()
            .remove(KEY_LAST_SCHOOL_SYNC)
            .remove(KEY_LAST_EMERGENCY_SYNC)
            .apply()
    }
}
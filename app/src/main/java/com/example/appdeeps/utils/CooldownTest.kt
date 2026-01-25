package com.example.appdeeps.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.appdeeps.cache.SyncCooldownManager

/**
 * COOLDOWN TEST UTILITY
 * For testing cooldown functionality
 */
object CooldownTest {

    /**
     * Force set cooldown for testing
     */
    fun setTestCooldown(context: Context, minutesAgo: Long) {
        val sharedPrefs = context.getSharedPreferences("sync_cooldown", Context.MODE_PRIVATE)
        val testTime = System.currentTimeMillis() - (minutesAgo * 60 * 1000)

        sharedPrefs.edit()
            .putLong("last_school_sync_time", testTime)
            .putLong("last_emergency_sync_time", testTime)
            .apply()
    }

    /**
     * Reset all cooldowns
     */
    fun resetAllCooldowns(context: Context) {
        val sharedPrefs = context.getSharedPreferences("sync_cooldown", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        val schoolCache = context.getSharedPreferences("appdeeps_cache", Context.MODE_PRIVATE)
        schoolCache.edit().clear().apply()
    }

    /**
     * Print cooldown status
     */
    suspend fun printCooldownStatus(context: Context) {
        val cooldownManager = SyncCooldownManager(context)

        val canSyncSchool = cooldownManager.canSyncSchools()
        val schoolMinutes = cooldownManager.getSchoolSyncCooldownMinutes()

        val canSyncEmergency = cooldownManager.canSyncEmergency()
        val emergencyMinutes = cooldownManager.getEmergencySyncCooldownMinutes()

        println("📊 COOLDOWN STATUS:")
        println("  Schools Sync: ${if (canSyncSchool) "✅ Allowed" else "⏸️ Cooldown ($schoolMinutes min remaining)"}")
        println("  Emergency Sync: ${if (canSyncEmergency) "✅ Allowed" else "⏸️ Cooldown ($emergencyMinutes min remaining)"}")
    }
}
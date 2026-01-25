package com.example.appdeeps.cache

import android.content.Context
import android.content.SharedPreferences
import com.example.appdeeps.School
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.content.edit

/**
 * SIMPLE CACHE MANAGER
 * Uses SharedPreferences + Gson for easy offline caching
 * No Room, No SQLite, No complex setup!
 */
class SimpleCacheManager(private val context: Context) {

    private val sharedPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("appdeeps_cache", Context.MODE_PRIVATE)
    }

    private val gson = Gson()

    companion object {
        private const val KEY_SCHOOLS_CACHE = "schools_cache"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val CACHE_DURATION_HOURS = 24L  // Cache valid for 24 hours
    }

    /**
     * Get last sync time from SharedPreferences
     */
    private fun getLastSyncTime(): Long {
        return sharedPrefs.getLong(KEY_LAST_SYNC_TIME, 0)
    }

    /**
     * Check if cache is expired (older than 24 hours)
     */
    fun isCacheExpired(): Boolean {
        val lastSync = getLastSyncTime()
        if (lastSync == 0L) return true  // Never synced

        val currentTime = System.currentTimeMillis()
        val hoursDiff = (currentTime - lastSync) / (1000 * 60 * 60)

        return hoursDiff >= CACHE_DURATION_HOURS
    }

    /**
     * Save schools to cache
     */
    suspend fun saveSchoolsToCache(schools: List<School>) {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = gson.toJson(schools)
                sharedPrefs.edit {
                    putString(KEY_SCHOOLS_CACHE, jsonString)
                    putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis())
                }

                println("✅ Cached ${schools.size} schools successfully")
            } catch (e: Exception) {
                println("❌ Failed to cache schools: ${e.message}")
            }
        }
    }

    /**
     * Get schools from cache
     */
    suspend fun getSchoolsFromCache(): List<School> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = sharedPrefs.getString(KEY_SCHOOLS_CACHE, null)
                if (jsonString.isNullOrEmpty()) {
                    return@withContext emptyList()
                }

                val type = object : TypeToken<List<School>>() {}.type
                val schools = gson.fromJson<List<School>>(jsonString, type) ?: emptyList()

                println("✅ Loaded ${schools.size} schools from cache")
                schools
            } catch (e: Exception) {
                println("❌ Failed to load from cache: ${e.message}")
                emptyList()
            }
        }
    }

    /**
     * Clear cache
     */
    fun clearCache() {
        sharedPrefs.edit { clear() }
        println("✅ Cache cleared")
    }

    /**
     * Get cache info
     */
    fun getCacheInfo(): CacheInfo {
        val lastSync = getLastSyncTime()
        val jsonString = sharedPrefs.getString(KEY_SCHOOLS_CACHE, "")
        val hasCache = !jsonString.isNullOrEmpty()

        return CacheInfo(
            hasData = hasCache,
            lastSynced = lastSync,
            isExpired = isCacheExpired()
        )
    }

    /**
     * Force cache expiration (for testing)
     */
    fun expireCache() {
        val expiredTime = System.currentTimeMillis() - (CACHE_DURATION_HOURS + 1) * 60 * 60 * 1000
        sharedPrefs.edit {
            putLong(KEY_LAST_SYNC_TIME, expiredTime)
        }
    }

    suspend fun getSyncCooldownInfo(): SyncCooldownInfo {
        val lastSync = getLastSyncTime()
        val currentTime = System.currentTimeMillis()

        return if (lastSync == 0L) {
            SyncCooldownInfo(
                canSync = true,
                minutesRemaining = 0L,
                lastSyncTime = 0L
            )
        } else {
            val minutesPassed = (currentTime - lastSync) / (1000 * 60)
            val minutesRemaining = CACHE_DURATION_HOURS * 60 - minutesPassed

            // Fixed: Explicitly convert to Long for comparison
            val canSync = minutesRemaining <= 0
            val finalMinutesRemaining = if (minutesRemaining < 0) 0L else minutesRemaining

            SyncCooldownInfo(
                canSync = canSync,
                minutesRemaining = finalMinutesRemaining,
                lastSyncTime = lastSync
            )
        }
    }
}

/**
 * Simple cache info data class
 */
data class CacheInfo(
    val hasData: Boolean,
    val lastSynced: Long,
    val isExpired: Boolean
)

data class SyncCooldownInfo(
    val canSync: Boolean,
    val minutesRemaining: Long,
    val lastSyncTime: Long
)
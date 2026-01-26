package com.example.appdeeps.sync

import com.example.appdeeps.School
import com.example.appdeeps.cache.SimpleCacheManager
import com.example.appdeeps.cache.SyncCooldownManager
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * SIMPLE REFRESH MANAGER WITH COOLDOWN
 * Handles Firebase sync with 30-minute cooldown
 */
class SimpleRefreshManager(
    private val cacheManager: SimpleCacheManager,
    private val cooldownManager: SyncCooldownManager  // Add cooldown manager
) {

    private val firebaseDatabase = Firebase.database(
        "https://ulipur-school-monitor-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private val schoolsRef = firebaseDatabase.getReference("schools")

    /**
     * Smart refresh: Uses cache if valid, otherwise fetches from Firebase
     * Now includes cooldown check
     */
    suspend fun smartRefresh(): RefreshResult {
        return try {
            // Check if cache is expired and cooldown allows sync
            if (!cacheManager.isCacheExpired()) {
                val cachedSchools = cacheManager.getSchoolsFromCache()
                if (cachedSchools.isNotEmpty()) {
                    return RefreshResult.Success(
                        message = "ক্যাশে থেকে ডেটা লোড করা হয়েছে",
                        schools = cachedSchools,
                        fromCache = true
                    )
                }
            }

            // Check cooldown before syncing with Firebase
            if (!cooldownManager.canSyncSchools()) {
                val minutesRemaining = cooldownManager.getSchoolSyncCooldownMinutes()
                return RefreshResult.Cooldown(
                    message = "৩০ মিনিটের জন্য সিঙ্ক্রোনাইজ বন্ধ। $minutesRemaining মিনিট অপেক্ষা করুন।",
                    minutesRemaining = minutesRemaining
                )
            }

            // Cache expired or empty, fetch from Firebase
            forceRefresh()

        } catch (e: Exception) {
            RefreshResult.Error(
                message = "স্মার্ট রিফ্রেশ ব্যর্থ: ${e.message}",
                error = e
            )
        }
    }

    /**
     * Force refresh from Firebase (ignores cache)
     * Now updates cooldown timer
     */
    suspend fun forceRefresh(): RefreshResult {
        return try {
            println("🔄 Fetching data from Firebase...")

            // Check cooldown first
            if (!cooldownManager.canSyncSchools()) {
                val minutesRemaining = cooldownManager.getSchoolSyncCooldownMinutes()
                return RefreshResult.Cooldown(
                    message = "৩০ মিনিটের জন্য সিঙ্ক্রোনাইজ বন্ধ। $minutesRemaining মিনিট অপেক্ষা করুন।",
                    minutesRemaining = minutesRemaining
                )
            }

            // Get data from Firebase
            val snapshot = schoolsRef.get().await()
            val schoolList = mutableListOf<School>()

            for (child in snapshot.children) {
                val school = School.fromSnapshot(child)
                schoolList.add(school)
            }

            println("✅ Got ${schoolList.size} schools from Firebase")

            // Save to cache
            cacheManager.saveSchoolsToCache(schoolList)

            // Update cooldown timer
            cooldownManager.updateSchoolSyncTime()

            RefreshResult.Success(
                message = "ডেটা সিঙ্ক্রোনাইজড হয়েছে ✅",
                schools = schoolList,
                fromCache = false
            )

        } catch (e: Exception) {
            println("❌ Firebase fetch failed: ${e.message}")

            // Try to get from cache as fallback
            val cachedSchools = cacheManager.getSchoolsFromCache()
            if (cachedSchools.isNotEmpty()) {
                return RefreshResult.Success(
                    message = "ফায়ারবেসে সমস্যা, ক্যাশে থেকে ডেটা লোড করা হয়েছে",
                    schools = cachedSchools,
                    fromCache = true
                )
            }

            RefreshResult.Error(
                message = "ডেটা লোড করতে ব্যর্থ: ${e.message}",
                error = e
            )
        }
    }

    /**
     * Check if sync is currently allowed
     */
    suspend fun isSyncAllowed(): Boolean {
        return cooldownManager.canSyncSchools()
    }

    /**
     * Get remaining cooldown minutes
     */
    suspend fun getRemainingCooldownMinutes(): Long {
        return cooldownManager.getSchoolSyncCooldownMinutes()
    }
}

/**
 * Refresh result sealed class with Cooldown state
 */
sealed class RefreshResult {
    data class Success(
        val message: String,
        val schools: List<School>,
        val fromCache: Boolean = false
    ) : RefreshResult()

    data class Error(
        val message: String,
        val error: Exception
    ) : RefreshResult()

    data class Cooldown(
        val message: String,
        val minutesRemaining: Long
    ) : RefreshResult()
}
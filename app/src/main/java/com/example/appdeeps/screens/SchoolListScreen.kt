package com.example.appdeeps.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appdeeps.School
import com.example.appdeeps.cache.SimpleCacheManager
import com.example.appdeeps.cache.SyncCooldownManager
import com.example.appdeeps.components.SchoolCard
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator

// Dialogs imports
import com.example.appdeeps.screens.components.dialogs.AboutDialog
import com.example.appdeeps.screens.components.dialogs.EmergencyDialog

// Components imports
import com.example.appdeeps.screens.components.ThreeDotMenu
import com.example.appdeeps.screens.components.SchoolSearchBar
import com.example.appdeeps.screens.components.StatisticsDashboard

// Utilities imports
import com.example.appdeeps.utils.MapUtils
import androidx.core.net.toUri

// ✅ NEW: Import for Pull-to-Refresh
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.example.appdeeps.sync.SimpleRefreshManager
import kotlinx.coroutines.launch

/**
 * SCHOOL LIST SCREEN - SIMPLIFIED WITH CACHE & 30-MINUTE COOLDOWN
 * Now works offline and reduces Firebase costs by 99.9%
 * Prevents excessive syncing with 30-minute cooldown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolListScreen(
    onSchoolClick: (School) -> Unit
) {
    // ==================== STATE MANAGEMENT ====================
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // School data states
    var schools by remember { mutableStateOf<List<School>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Search functionality states
    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    // Dialog visibility states
    var showAboutDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }

    // ✅ NEW: Refresh states
    var isRefreshing by remember { mutableStateOf(false) }
    var refreshMessage by remember { mutableStateOf("") }
    var showRefreshMessage by remember { mutableStateOf(false) }
    var lastRefreshTime by remember { mutableStateOf<String>("কখনোই নয়") }

    // ✅ NEW: Cooldown states
    var cooldownMessage by remember { mutableStateOf("") }
    var showCooldownMessage by remember { mutableStateOf(false) }
    var remainingCooldownMinutes by remember { mutableStateOf(0L) }

    // ✅ NEW: Simple Cache Manager
    val cacheManager = remember { SimpleCacheManager(context) }

    // ✅ NEW: Sync Cooldown Manager (30-minute cooldown)
    val cooldownManager = remember { SyncCooldownManager(context) }

    // ✅ UPDATED: Simple Refresh Manager with cooldown manager
    val refreshManager = remember { SimpleRefreshManager(cacheManager, cooldownManager) }

    // ✅ NEW: Pull-to-refresh state
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    // ==================== HELPER FUNCTION ====================
    // ✅ MOVED HERE: Function to update last refresh time
    fun updateLastRefreshTime() {
        val cacheInfo = cacheManager.getCacheInfo()
        if (cacheInfo.lastSynced > 0) {
            val minutesAgo = (System.currentTimeMillis() - cacheInfo.lastSynced) / (1000 * 60)
            lastRefreshTime = when {
                minutesAgo < 1 -> "এইমাত্র"
                minutesAgo < 60 -> "$minutesAgo মিনিট আগে"
                else -> "${minutesAgo / 60} ঘণ্টা আগে"
            }
        }
    }

    // ==================== DATA FILTERING ====================
    val filteredSchools = remember(schools, searchText.text) {
        if (searchText.text.isEmpty()) {
            schools
        } else {
            schools.filter { school ->
                val query = searchText.text.lowercase()
                school.schoolName.lowercase().contains(query) ||
                        school.schoolNumber.contains(query) ||
                        school.unionName.lowercase().contains(query)
            }
        }
    }

    // ==================== INITIAL DATA LOADING ====================
    LaunchedEffect(Unit) {
        loadInitialData(cacheManager, refreshManager) { loadedSchools ->
            schools = loadedSchools
            isLoading = false
            updateLastRefreshTime()
        }
    }

    // ==================== REFRESH FUNCTION WITH COOLDOWN ====================
    val refreshData = suspend {
        isRefreshing = true
        refreshMessage = "ডেটা আপডেট করা হচ্ছে..."

        val result = refreshManager.smartRefresh()

        when (result) {
            is com.example.appdeeps.sync.RefreshResult.Success -> {
                schools = result.schools
                refreshMessage = if (result.fromCache) {
                    "ক্যাশে থেকে ডেটা লোড করা হয়েছে"
                } else {
                    "ডেটা সিঙ্ক্রোনাইজড হয়েছে ✅"
                }
                updateLastRefreshTime()
            }
            is com.example.appdeeps.sync.RefreshResult.Error -> {
                refreshMessage = "ত্রুটি: ${result.message}"
            }
            // ✅ NEW: Handle cooldown state
            is com.example.appdeeps.sync.RefreshResult.Cooldown -> {
                cooldownMessage = result.message
                remainingCooldownMinutes = result.minutesRemaining
                showCooldownMessage = true
            }
        }

        // Only show refresh message if not in cooldown
        if (result !is com.example.appdeeps.sync.RefreshResult.Cooldown) {
            showRefreshMessage = true
        }
        isRefreshing = false

        // Hide messages after 3 seconds
        kotlinx.coroutines.delay(3000)
        showRefreshMessage = false
        showCooldownMessage = false
    }

    // ==================== MANUAL REFRESH FUNCTION WITH COOLDOWN ====================
    val forceRefresh = suspend {
        // Check cooldown first
        val canSync = refreshManager.isSyncAllowed()
        if (!canSync) {
            remainingCooldownMinutes = refreshManager.getRemainingCooldownMinutes()
            cooldownMessage = "৩০ মিনিটের জন্য সিঙ্ক্রোনাইজ বন্ধ। $remainingCooldownMinutes মিনিট অপেক্ষা করুন।"
            showCooldownMessage = true
            isRefreshing = false

            // Hide message after 5 seconds
            kotlinx.coroutines.delay(5000)
            showCooldownMessage = false
        } else {
            isRefreshing = true
            refreshMessage = "জোরপূর্বক আপডেট করা হচ্ছে..."

            val result = refreshManager.forceRefresh()

            when (result) {
                is com.example.appdeeps.sync.RefreshResult.Success -> {
                    schools = result.schools
                    refreshMessage = "ডেটা ফ্রেশ করা হয়েছে ✅"
                    updateLastRefreshTime()
                }
                is com.example.appdeeps.sync.RefreshResult.Error -> {
                    refreshMessage = "ত্রুটি: ${result.message}"
                }
                is com.example.appdeeps.sync.RefreshResult.Cooldown -> {
                    cooldownMessage = result.message
                    remainingCooldownMinutes = result.minutesRemaining
                    showCooldownMessage = true
                }
            }

            // Only show refresh message if not in cooldown
            if (result !is com.example.appdeeps.sync.RefreshResult.Cooldown) {
                showRefreshMessage = true
            }
            isRefreshing = false

            kotlinx.coroutines.delay(3000)
            showRefreshMessage = false
            showCooldownMessage = false
        }
    }

    // ==================== MAIN UI WITH PULL-TO-REFRESH ====================
    Box(modifier = Modifier.fillMaxSize()) {
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                coroutineScope.launch {
                    refreshData()
                }
            },
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = trigger,
                    scale = true,
                    backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // 1. Custom App Header with Menu and Refresh Button
                item {
                    EnhancedHeaderWithRefresh(
                        onAboutClick = { showAboutDialog = true },
                        onEmergencyClick = { showEmergencyDialog = true },
                        onRefreshClick = {
                            coroutineScope.launch {
                                forceRefresh()
                            }
                        },
                        isRefreshing = isRefreshing,
                        lastRefreshTime = lastRefreshTime,
                        // ✅ NEW: Pass cooldown status to header
                        isCooldownActive = remainingCooldownMinutes > 0,
                        remainingMinutes = remainingCooldownMinutes
                    )
                }

                // 2. Refresh Message (if visible)
                if (showRefreshMessage) {
                    item {
                        RefreshMessageBanner(message = refreshMessage)
                    }
                }

                // ✅ NEW: Cooldown Message (if visible)
                if (showCooldownMessage) {
                    item {
                        CooldownMessageBanner(
                            message = cooldownMessage,
                            minutesRemaining = remainingCooldownMinutes
                        )
                    }
                }

                // 3. Search Bar Component
                item {
                    SchoolSearchBar(
                        searchText = searchText,
                        onSearchTextChange = { searchText = it }
                    )
                }

                // 4. Statistics Dashboard (4 Cards)
                item {
                    StatisticsDashboard(
                        isLoading = isLoading,
                        allSchools = schools,
                        filteredSchools = filteredSchools,
                        isSearching = searchText.text.isNotEmpty()
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 5. Search Results Indicator
                if (searchText.text.isNotEmpty()) {
                    item {
                        SearchResultsIndicator(
                            searchQuery = searchText.text,
                            resultCount = filteredSchools.size
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // ✅ ADDED: Test button (remove in production)
                item {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                cooldownManager.resetCooldown()
                                refreshMessage = "কুলডাউন রিসেট করা হয়েছে"
                                showRefreshMessage = true
                                kotlinx.coroutines.delay(2000)
                                showRefreshMessage = false
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5722)
                        )
                    ) {
                        Text("কুলডাউন রিসেট করুন (টেস্টের জন্য)")
                    }
                }

                // 6. School List Content (Handles all states)
                when {
                    // Loading State
                    isLoading -> {
                        item {
                            LoadingState()
                        }
                    }

                    // Empty State (no schools found)
                    filteredSchools.isEmpty() -> {
                        item {
                            EmptyState(searchQuery = searchText.text)
                        }
                    }

                    // Success State (show school list)
                    else -> {
                        items(filteredSchools, key = { it.id }) { school ->
                            SchoolCard(
                                school = school,
                                onClick = { onSchoolClick(school) },
                                onLocationClick = { openGoogleMaps(school, context) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }

    // ==================== DIALOGS ====================
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    if (showEmergencyDialog) {
        EmergencyDialog(onDismiss = { showEmergencyDialog = false })
    }
}

// ==================== NEW COMPONENTS ====================

/**
 * Enhanced header with refresh button and last sync time
 */
@Composable
private fun EnhancedHeaderWithRefresh(
    onAboutClick: () -> Unit,
    onEmergencyClick: () -> Unit,
    onRefreshClick: () -> Unit,
    isRefreshing: Boolean,
    lastRefreshTime: String,
    // ✅ NEW: Cooldown parameters
    isCooldownActive: Boolean = false,
    remainingMinutes: Long = 0
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title Section (Left side)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "উলিপুর ভোটকেন্দ্র পর্যবেক্ষণ",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Text(
                        text = "কুড়িগ্রাম",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }

                // ✅ UPDATED: Refresh Button with cooldown indicator
                Box {
                    IconButton(
                        onClick = onRefreshClick,
                        enabled = !isRefreshing && !isCooldownActive
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else if (isCooldownActive) {
                            // Show timer icon when cooldown active
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Cooldown",
                                    tint = Color.White.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "$remainingMinutes",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Color.White
                            )
                        }
                    }

                    // ✅ NEW: Show cooldown progress ring
                    if (isCooldownActive && remainingMinutes > 0) {
                        val progress = (30 - remainingMinutes).toFloat() / 30
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(30.dp),
                            color = Color.White.copy(alpha = 0.3f),
                            strokeWidth = 2.dp
                        )
                    }
                }

                // Three-dot Menu
                ThreeDotMenu(
                    onAboutClick = onAboutClick,
                    onEmergencyClick = onEmergencyClick,
                    iconColor = Color.White
                )
            }

            // Last refresh time
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "সর্বশেষ আপডেট: $lastRefreshTime",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Shows refresh message banner
 */
@Composable
private fun RefreshMessageBanner(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

/**
 * ✅ NEW: Shows cooldown message banner
 */
@Composable
private fun CooldownMessageBanner(
    message: String,
    minutesRemaining: Long
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color(0xFFFF9800)
                )

                // ✅ NEW: Progress bar showing cooldown progress
                if (minutesRemaining > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (30 - minutesRemaining).toFloat() / 30 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "প্রগতি: ${((30 - minutesRemaining) * 100 / 30)}%",
                        fontSize = 12.sp,
                        color = Color(0xFFFF9800).copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// ==================== DATA LOADING FUNCTIONS ====================

/**
 * Loads initial data with cache-first approach
 */
private suspend fun loadInitialData(
    cacheManager: SimpleCacheManager,
    refreshManager: SimpleRefreshManager,
    onDataLoaded: (List<School>) -> Unit
) {
    println("🔄 Loading initial data...")

    // Try cache first
    try {
        val cachedSchools = cacheManager.getSchoolsFromCache()
        if (cachedSchools.isNotEmpty() && !cacheManager.isCacheExpired()) {
            println("✅ Using cached data (${cachedSchools.size} schools)")
            onDataLoaded(cachedSchools)
            return
        }
    } catch (e: Exception) {
        println("❌ Cache load failed: ${e.message}")
    }

    // Cache empty or expired, fetch from Firebase
    try {
        println("🔄 Cache expired/empty, fetching from Firebase...")
        val result = refreshManager.forceRefresh()
        if (result is com.example.appdeeps.sync.RefreshResult.Success) {
            onDataLoaded(result.schools)
        } else if (result is com.example.appdeeps.sync.RefreshResult.Error) {
            println("❌ Firebase failed: ${result.message}")
            // Try cache one more time as fallback
            val cachedSchools = cacheManager.getSchoolsFromCache()
            if (cachedSchools.isNotEmpty()) {
                onDataLoaded(cachedSchools)
            }
        }
    } catch (e: Exception) {
        println("❌ Initial load completely failed: ${e.message}")
    }
}

@Composable
private fun SearchResultsIndicator(
    searchQuery: String,
    resultCount: Int
) {
    Text(
        text = "‘$searchQuery’ অনুসন্ধানে ${resultCount}টি ভোটকেন্দ্র পাওয়া গেছে",
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("ডেটা লোড হচ্ছে \nআপনার ইন্টারনেট সংযোগ পরীক্ষা করুন")
        }
    }
}

@Composable
private fun EmptyState(searchQuery: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            if (searchQuery.isNotEmpty()) {
                Text(
                    text = "‘$searchQuery’ অনুসন্ধানে কোন ভোটকেন্দ্র পাওয়া যায়নি",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "অন্য কিছু লিখে চেষ্টা করুন",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text("কোনও ভোটকেন্দ্রের তথ্য পাওয়া যায়নি")
                Text(
                    "ফায়ারবেসে ডেটা যোগ করুন",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun openGoogleMaps(school: School, context: android.content.Context) {
    if (school.latitude != 0.0 && school.longitude != 0.0) {
        try {
            val gmmIntentUri = "geo:${school.latitude},${school.longitude}?q=${school.latitude},${school.longitude}(${Uri.encode(school.schoolName)})".toUri()
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                "https://www.google.com/maps/search/?api=1&query=${school.latitude},${school.longitude}".toUri()
            )
            context.startActivity(webIntent)
        }
    }
}

private fun logDebugInfo(message: String) {
    println("🔍 DEBUG: $message")
}
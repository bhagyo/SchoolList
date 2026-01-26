package com.example.appdeeps.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.appdeeps.School
import com.example.appdeeps.cache.SimpleCacheManager
import com.example.appdeeps.cache.SyncCooldownManager
import com.example.appdeeps.components.SchoolCard
import com.example.appdeeps.screens.components.SchoolSearchBar
import com.example.appdeeps.screens.components.StatisticsDashboard
import com.example.appdeeps.screens.components.ThreeDotMenu
import com.example.appdeeps.screens.components.dialogs.AboutDialog
import com.example.appdeeps.screens.components.dialogs.EmergencyDialog
import com.example.appdeeps.sync.SimpleRefreshManager
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolListScreen(
    onSchoolClick: (School) -> Unit
) {
    // ------------------ BASIC STATE ------------------
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var schools by remember { mutableStateOf<List<School>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    // ------------------ UI STATE ------------------
    var isRefreshing by remember { mutableStateOf(false) }
    var showRefreshMessage by remember { mutableStateOf(false) }
    var refreshMessage by remember { mutableStateOf("") }

    var showCooldownMessage by remember { mutableStateOf(false) }
    var cooldownMessage by remember { mutableStateOf("") }
    var remainingCooldownMinutes by remember { mutableStateOf(0L) }

    var lastRefreshTime by remember { mutableStateOf("কখনোই নয়") }

    var showAboutDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }

    // ------------------ MANAGERS ------------------
    val cacheManager = remember { SimpleCacheManager(context) }
    val cooldownManager = remember { SyncCooldownManager(context) }
    val refreshManager = remember { SimpleRefreshManager(cacheManager, cooldownManager) }

    val swipeState = rememberSwipeRefreshState(isRefreshing)

    // ------------------ HELPERS ------------------
    fun updateLastRefreshTime() {
        val info = cacheManager.getCacheInfo()
        if (info.lastSynced > 0) {
            val mins = (System.currentTimeMillis() - info.lastSynced) / 60000
            lastRefreshTime = when {
                mins < 1 -> "এইমাত্র"
                mins < 60 -> "$mins মিনিট আগে"
                else -> "${mins / 60} ঘণ্টা আগে"
            }
        }
    }

    // ------------------ INITIAL LOAD ------------------
    LaunchedEffect(Unit) {
        try {
            val cached = cacheManager.getSchoolsFromCache()
            if (cached.isNotEmpty() && !cacheManager.isCacheExpired()) {
                schools = cached
            } else {
                val result = refreshManager.forceRefresh()
                if (result is com.example.appdeeps.sync.RefreshResult.Success) {
                    schools = result.schools
                }
            }
        } catch (_: Exception) {}
        isLoading = false
        updateLastRefreshTime()
    }

    // ------------------ REFRESH LOGIC (FIXED) ------------------
    suspend fun handleRefresh() {
        if (isRefreshing) return
        isRefreshing = true

        // COOLDOWN CHECK
        if (!refreshManager.isSyncAllowed()) {
            remainingCooldownMinutes = refreshManager.getRemainingCooldownMinutes()
            cooldownMessage =
                "৩০ মিনিটের জন্য সিঙ্ক্রোনাইজ বন্ধ। $remainingCooldownMinutes মিনিট অপেক্ষা করুন।"
            showCooldownMessage = true
            isRefreshing = false
            delay(10000)
            showCooldownMessage = false
            return
        }

        refreshMessage = "ডেটা আপডেট করা হচ্ছে..."
        showRefreshMessage = true

        when (val result = refreshManager.forceRefresh()) {
            is com.example.appdeeps.sync.RefreshResult.Success -> {
                schools = result.schools
                refreshMessage = "ডেটা সিঙ্ক্রোনাইজড হয়েছে ✅"
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

        isRefreshing = false
        delay(10000)
        showRefreshMessage = false
        showCooldownMessage = false
    }

    // ------------------ FILTER ------------------
    val filteredSchools = remember(schools, searchText.text) {
        if (searchText.text.isEmpty()) schools
        else schools.filter {
            it.schoolName.contains(searchText.text, true)
        }
    }

    // ------------------ UI ------------------
    Box(Modifier.fillMaxSize()) {
        SwipeRefresh(
            state = swipeState,
            onRefresh = { scope.launch { handleRefresh() } },
            indicator = { s, t ->
                SwipeRefreshIndicator(
                    state = s,
                    refreshTriggerDistance = t,
                    scale = true
                )
            }
        ) {
            LazyColumn {
                item {
                    Header(
                        isRefreshing = isRefreshing,
                        lastRefreshTime = lastRefreshTime,
                        onRefreshClick = { scope.launch { handleRefresh() } },
                        onAbout = { showAboutDialog = true },
                        onEmergency = { showEmergencyDialog = true }
                    )
                }

                if (showRefreshMessage) {
                    item { MessageBanner(refreshMessage) }
                }

                if (showCooldownMessage) {
                    item { CooldownBanner(cooldownMessage, remainingCooldownMinutes) }
                }

                item {
                    SchoolSearchBar(
                        modifier = Modifier.fillMaxWidth(),
                        searchText = searchText,
                        onSearchTextChange = { searchText = it }
                    )
                }

                item {
                    StatisticsDashboard(
                        isLoading = isLoading,
                        allSchools = schools,
                        filteredSchools = filteredSchools,
                        isSearching = searchText.text.isNotEmpty()
                    )
                }

                when {
                    isLoading -> item { LoadingState() }
                    filteredSchools.isEmpty() -> item { EmptyState(searchText.text) }
                    else -> items(filteredSchools) { school ->
                        SchoolCard(
                            school = school,
                            onClick = { onSchoolClick(school) },
                            onLocationClick = { openGoogleMaps(school, context) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (showAboutDialog) AboutDialog { showAboutDialog = false }
    if (showEmergencyDialog) EmergencyDialog { showEmergencyDialog = false }
}

// ------------------ COMPONENTS ------------------

@Composable
private fun Header(
    isRefreshing: Boolean,
    lastRefreshTime: String,
    onRefreshClick: () -> Unit,
    onAbout: () -> Unit,
    onEmergency: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("উলিপুর ভোটকেন্দ্র পর্যবেক্ষণ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("সর্বশেষ আপডেট: $lastRefreshTime", fontSize = 12.sp, color = Color.White.copy(.8f))
            }
            IconButton(onClick = onRefreshClick, enabled = !isRefreshing) {
                if (isRefreshing) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                else Icon(Icons.Default.Refresh, null, tint = Color.White)
            }
            ThreeDotMenu(
                modifier = Modifier,
                onAboutClick = onAbout,
                onEmergencyClick = onEmergency,
                iconColor = Color.White
            )
        }
    }
}

@Composable
private fun MessageBanner(text: String) {
    Card(Modifier.padding(16.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Refresh, null)
            Spacer(Modifier.width(8.dp))
            Text(text)
        }
    }
}

@Composable
private fun CooldownBanner(text: String, minutes: Long) {
    Card(
        Modifier.padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = Color(0xFFFF9800))
                Spacer(Modifier.width(8.dp))
                Text(text, color = Color(0xFFFF9800))
            }
            LinearProgressIndicator(
                progress = { (30 - minutes).coerceAtLeast(0).toFloat() / 30 },
                color = Color(0xFFFF9800)
            )
        }
    }
}

// ------------------ STATES ------------------

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(query: String) {
    Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) {
        Text(if (query.isEmpty()) "কোনো তথ্য নেই" else "ফলাফল পাওয়া যায়নি")
    }
}

// ------------------ MAP ------------------

private fun openGoogleMaps(school: School, context: android.content.Context) {
    if (school.latitude == 0.0 || school.longitude == 0.0) return
    val uri =
        "geo:${school.latitude},${school.longitude}?q=${school.latitude},${school.longitude}".toUri()
    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
}

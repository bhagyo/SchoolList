package com.example.appdeeps.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appdeeps.School
import com.example.appdeeps.components.SchoolCard
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem

// Add these imports right after the existing imports:
import com.example.appdeeps.screens.components.dialogs.AboutDialog
import com.example.appdeeps.screens.components.dialogs.EmergencyDialog
//For tackling the three dot menu in top right
import com.example.appdeeps.screens.components.ThreeDotMenu
import com.example.appdeeps.screens.components.SchoolSearchBar

//Utilities import
import com.example.appdeeps.screens.components.StatisticsDashboard
import com.example.appdeeps.screens.components.SchoolListHeader

// Add these import lines:
import com.example.appdeeps.utils.MapUtils
import androidx.core.net.toUri


/**
 * SCHOOL LIST SCREEN
 *
 * Main screen for displaying and managing all schools in Ulipur.
 * Features:
 * - Firebase data loading with real-time updates
 * - Search functionality (by name, number, or union)
 * - Statistics dashboard with 4 key metrics
 * - Menu with About and Emergency dialogs
 * - Interactive school cards with map navigation
 * - Filtered vs total statistics display
 *
 * @param onSchoolClick Callback when a school is clicked to navigate to details
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolListScreen(
    onSchoolClick: (School) -> Unit
) {
    // ==================== STATE MANAGEMENT ====================
    val context = LocalContext.current

    // School data states
    var schools by remember { mutableStateOf<List<School>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Search functionality states
    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    // Dialog visibility states
    var showAboutDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }

    // ==================== DATA FILTERING ====================
    /**
     * Filters schools based on search text across multiple fields:
     * - School name (case-insensitive, supports Bangla)
     * - School number
     * - Union name
     */
    val filteredSchools = remember(schools, searchText.text) {
        if (searchText.text.isEmpty()) {
            schools // Return all schools when no search query
        } else {
            schools.filter { school ->
                val query = searchText.text.lowercase()
                school.schoolName.lowercase().contains(query) ||
                        school.schoolNumber.contains(query) ||
                        school.unionName.lowercase().contains(query)
            }
        }
    }

    // ==================== FIREBASE DATA LOADING ====================
    LaunchedEffect(Unit) {
        loadSchoolsFromFirebase(
            onSchoolsLoaded = { loadedSchools ->
                schools = loadedSchools
                isLoading = false
            },
            onError = { error ->
                isLoading = false
                println("❌ Firebase error: $error")
            }
        )
    }

    // ==================== MAIN UI STRUCTURE ====================
    // Everything inside LazyColumn so it scrolls away
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // 1. Custom App Header with Menu (still sticky if you want, or remove it)
        item {
            SchoolListHeader(
                onAboutClick = { showAboutDialog = true },
                onEmergencyClick = { showEmergencyDialog = true }
            )
        }

        // 2. Search Bar Component
        item {
            SchoolSearchBar(
                searchText = searchText,
                onSearchTextChange = { searchText = it }
            )
        }

        // 3. Statistics Dashboard (4 Cards)
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

        // 4. Search Results Indicator
        if (searchText.text.isNotEmpty()) {
            item {
                SearchResultsIndicator(
                    searchQuery = searchText.text,
                    resultCount = filteredSchools.size
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // 5. School List Content (Handles all states)
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

    // ==================== DIALOGS ====================
    // About Us Dialog
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    // Emergency Numbers Dialog
    if (showEmergencyDialog) {
        EmergencyDialog(onDismiss = { showEmergencyDialog = false })
    }
}

// ==================== FIREBASE DATA LOADER ====================

/**
 * Loads school data from Firebase Realtime Database.
 * Uses ValueEventListener for real-time updates.
 *
 * @param onSchoolsLoaded Callback when schools are successfully loaded
 * @param onError Callback when Firebase returns an error
 */
private fun loadSchoolsFromFirebase(
    onSchoolsLoaded: (List<School>) -> Unit,
    onError: (String) -> Unit
) {
    val database = Firebase.database(
        "https://ulipur-school-monitor-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    val schoolsRef = database.getReference("schools")

    schoolsRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val schoolList = mutableListOf<School>()

            logDebugInfo("Got snapshot with ${snapshot.childrenCount} children")
            logDebugInfo("Snapshot exists: ${snapshot.exists()}")

            // Iterate through all child nodes
            for (child in snapshot.children) {
                val school = child.getValue(School::class.java)
                if (school != null) {
                    logDebugInfo("✅ Successfully parsed: ${school.schoolName}")
                    schoolList.add(school)
                } else {
                    logDebugInfo("❌ FAILED to parse school ${child.key}")
                }
            }

            onSchoolsLoaded(schoolList)

            // Final debug output
            logDebugInfo("Total schools loaded: ${schoolList.size}")
            logDebugInfo("First school: ${schoolList.firstOrNull()?.schoolName}")
        }

        override fun onCancelled(error: DatabaseError) {
            onError(error.message)
        }
    })
}

// ==================== UI COMPONENTS ====================

/**
 * Indicator showing search results count.
 *
 * @param searchQuery The current search query
 * @param resultCount Number of schools matching the search
 */
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

/**
 * Shows loading indicator while data is being fetched.
 */
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

/**
 * Shows appropriate message when no schools are found.
 *
 * @param searchQuery Current search query to show in message
 */
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

/**
 * Opens Google Maps for the given school location.
 * Falls back to browser if Google Maps is not installed.
 *
 * @param school The school with location coordinates
 * @param context Android context for starting activity
 */
private fun openGoogleMaps(school: School, context: android.content.Context) {
    if (school.latitude != 0.0 && school.longitude != 0.0) {
        try {
            // Try to open in Google Maps app
            val gmmIntentUri =
                "geo:${school.latitude},${school.longitude}?q=${school.latitude},${school.longitude}(${
                    Uri.encode(school.schoolName)
                })".toUri()
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            // Fallback to browser
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                "https://www.google.com/maps/search/?api=1&query=${school.latitude},${school.longitude}".toUri()
            )
            context.startActivity(webIntent)
        }
    }
}

/**
 * Logs debug information to console.
 *
 * @param message Debug message to log
 */
private fun logDebugInfo(message: String) {
    println("🔍 DEBUG: $message")
}
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
    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Custom App Header with Menu
        SchoolListHeader(
            onAboutClick = { showAboutDialog = true },
            onEmergencyClick = { showEmergencyDialog = true }
        )

        // 2. Search Bar Component
        SearchBar(
            searchText = searchText,
            onSearchTextChange = { searchText = it }
        )

        // 3. Statistics Dashboard (4 Cards)
        StatisticsDashboard(
            isLoading = isLoading,
            schools = schools,
            filteredSchools = filteredSchools,
            isSearching = searchText.text.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Search Results Indicator
        if (searchText.text.isNotEmpty()) {
            SearchResultsIndicator(
                searchQuery = searchText.text,
                resultCount = filteredSchools.size
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 5. School List Content (Handles all states)
        SchoolListContent(
            isLoading = isLoading,
            filteredSchools = filteredSchools,
            searchQuery = searchText.text,
            onSchoolClick = onSchoolClick,
            onLocationClick = { school -> openGoogleMaps(school, context) }
        )
    }

    // ==================== DIALOGS ====================
    AboutDialog(
        showDialog = showAboutDialog,
        onDismiss = { showAboutDialog = false }
    )

    EmergencyDialog(
        showDialog = showEmergencyDialog,
        onDismiss = { showEmergencyDialog = false }
    )
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
 * Custom header for School List Screen with title and menu.
 *
 * @param onAboutClick Callback for About menu item
 * @param onEmergencyClick Callback for Emergency Numbers menu item
 */
@Composable
private fun SchoolListHeader(
    onAboutClick: () -> Unit,
    onEmergencyClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title Section
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "উলিপুর বিদ্যালয় মনিটরিং",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "কুড়িগ্রাম, রংপুর",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }

            // Menu Section
            ThreeDotMenu(
                onAboutClick = onAboutClick,
                onEmergencyClick = onEmergencyClick
            )
        }
    }
}

/**
 * Three-dot dropdown menu with app options.
 */
@Composable
private fun ThreeDotMenu(
    onAboutClick: () -> Unit,
    onEmergencyClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("আমাদের সম্পর্কে") },
                onClick = {
                    expanded = false
                    onAboutClick()
                }
            )
            DropdownMenuItem(
                text = { Text("জরুরী নাম্বার") },
                onClick = {
                    expanded = false
                    onEmergencyClick()
                }
            )
        }
    }
}

/**
 * Search bar component with clear functionality.
 *
 * @param searchText Current search query
 * @param onSearchTextChange Callback when search text changes
 */
@Composable
private fun SearchBar(
    searchText: TextFieldValue,
    onSearchTextChange: (TextFieldValue) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Search TextField
            TextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "বিদ্যালয়ের নাম, নম্বর বা ইউনিয়ন খুঁজুন...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    disabledContainerColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            // Clear button (only visible when there's text)
            if (searchText.text.isNotEmpty()) {
                IconButton(onClick = { onSearchTextChange(TextFieldValue("")) }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Statistics dashboard showing 4 key metrics in cards.
 * Shows filtered values when search is active.
 *
 * @param isLoading Whether data is still loading
 * @param schools All schools (unfiltered)
 * @param filteredSchools Filtered schools based on search
 * @param isSearching Whether user is currently searching
 */
@Composable
private fun StatisticsDashboard(
    isLoading: Boolean,
    schools: List<School>,
    filteredSchools: List<School>,
    isSearching: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Row 1: Total Schools & Total Students
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(
                title = "মোট বিদ্যালয়",
                value = if (isLoading) "..." else schools.size.toString(),
                modifier = Modifier.weight(1f),
                showFiltered = false,
                searchActive = isSearching,
                filteredValue = filteredSchools.size.toString()
            )

            Spacer(modifier = Modifier.width(12.dp))

            StatCard(
                title = "মোট শিক্ষার্থী",
                value = if (isLoading) "..." else calculateTotalStudents(schools).toString(),
                modifier = Modifier.weight(1f),
                showFiltered = true,
                searchActive = isSearching,
                filteredValue = calculateTotalStudents(filteredSchools).toString()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 2: Total Attendance & Average Attendance
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(
                title = "মোট উপস্থিতি",
                value = if (isLoading) "..." else calculateTotalAttendance(schools).toString(),
                modifier = Modifier.weight(1f),
                showFiltered = true,
                searchActive = isSearching,
                filteredValue = calculateTotalAttendance(filteredSchools).toString()
            )

            Spacer(modifier = Modifier.width(12.dp))

            StatCard(
                title = "গড় উপস্থিতি",
                value = if (isLoading) "..." else "${calculateAverageAttendance(schools)}%",
                modifier = Modifier.weight(1f),
                showFiltered = true,
                searchActive = isSearching,
                filteredValue = "${calculateAverageAttendance(filteredSchools)}%"
            )
        }
    }
}

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
        text = "‘$searchQuery’ অনুসন্ধানে ${resultCount}টি বিদ্যালয় পাওয়া গেছে",
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

/**
 * Main content area that handles all display states.
 *
 * @param isLoading Whether data is loading
 * @param filteredSchools Schools to display (already filtered)
 * @param searchQuery Current search query (for empty state messages)
 * @param onSchoolClick Callback when school card is clicked
 * @param onLocationClick Callback when location icon is clicked
 */
@Composable
private fun SchoolListContent(
    isLoading: Boolean,
    filteredSchools: List<School>,
    searchQuery: String,
    onSchoolClick: (School) -> Unit,
    onLocationClick: (School) -> Unit
) {
    when {
        // Loading State
        isLoading -> {
            LoadingState()
        }

        // Empty State (no schools found)
        filteredSchools.isEmpty() -> {
            EmptyState(searchQuery = searchQuery)
        }

        // Success State (show school list)
        else -> {
            SchoolList(
                schools = filteredSchools,
                onSchoolClick = onSchoolClick,
                onLocationClick = onLocationClick
            )
        }
    }
}

/**
 * Shows loading indicator while data is being fetched.
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("ডেটা লোড হচ্ছে...")
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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            if (searchQuery.isNotEmpty()) {
                Text(
                    text = "‘$searchQuery’ অনুসন্ধানে কোন বিদ্যালয় পাওয়া যায়নি",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "অন্য কিছু লিখে চেষ্টা করুন",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text("কোনও বিদ্যালয়ের তথ্য পাওয়া যায়নি")
                Text(
                    "ফায়ারবেসে ডেটা যোগ করুন",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Displays list of schools in a scrollable column.
 *
 * @param schools List of schools to display
 * @param onSchoolClick Callback when school card is clicked
 * @param onLocationClick Callback when location icon is clicked
 */
@Composable
private fun SchoolList(
    schools: List<School>,
    onSchoolClick: (School) -> Unit,
    onLocationClick: (School) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(schools, key = { it.id }) { school ->
            SchoolCard(
                school = school,
                onClick = { onSchoolClick(school) },
                onLocationClick = { onLocationClick(school) }
            )
        }
    }
}

// ==================== DIALOG COMPONENTS ====================

/**
 * About dialog showing app information.
 */
@Composable
private fun AboutDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("আমাদের সম্পর্কে", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "এই অ্যাপটি উলিপুর বিদ্যালয় মনিটরিং সিস্টেমের জন্য তৈরি করা হয়েছে।\n\n" +
                            "উদ্দেশ্য:\n" +
                            "• সকল বিদ্যালয়ের উপস্থিতি মনিটরিং\n" +
                            "• শিক্ষকদের সাথে সহজ যোগাযোগ\n" +
                            "• বিদ্যালয়ের অবস্থান দেখা\n" +
                            "• জরুরী ক্ষেত্রে দ্রুত সাহায্য\n\n" +
                            "সংস্করণ: ১.০.০\n" +
                            "ডেভেলপার: উলিপুর ডিজিটাল টিম"
                )
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("ঠিক আছে")
                }
            }
        )
    }
}

/**
 * Emergency contacts dialog.
 */
@Composable
private fun EmergencyDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("জরুরী নাম্বার", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("জরুরী যোগাযোগের জন্য:", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• জাতীয় জরুরী সেবা: ৯৯৯")
                    Text("• থানা: ০১৭１３-XXX")
                    Text("• ফায়ার সার্ভিস: ０１７１-XXX")
                    Text("• এম্বুলেন্স: ০১৭০-XXX")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("উলিপুর উপজেলা:")
                    Text("• উপজেলা নির্বাহী অফিসার: XXX")
                    Text("• শিক্ষা অফিসার: XXX")
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("ঠিক আছে")
                }
            }
        )
    }
}

// ==================== HELPER FUNCTIONS ====================

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
            val gmmIntentUri = Uri.parse(
                "geo:${school.latitude},${school.longitude}?q=${school.latitude},${school.longitude}(${Uri.encode(school.schoolName)})"
            )
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            // Fallback to browser
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=${school.latitude},${school.longitude}")
            )
            context.startActivity(webIntent)
        }
    }
}

/**
 * Calculates average attendance percentage across schools.
 *
 * @param schools List of schools
 * @return Average attendance percentage (0 if empty list)
 */
private fun calculateAverageAttendance(schools: List<School>): Int {
    if (schools.isEmpty()) return 0
    val total = schools.sumOf { it.attendancePercentage }
    return total / schools.size
}

/**
 * Calculates total students across all schools.
 *
 * @param schools List of schools
 * @return Total number of students
 */
private fun calculateTotalStudents(schools: List<School>): Int {
    return schools.sumOf { it.totalStudents }
}

/**
 * Calculates total daily attendance across all schools.
 *
 * @param schools List of schools
 * @return Total daily attendance count
 */
private fun calculateTotalAttendance(schools: List<School>): Int {
    return schools.sumOf { it.dailyAttendance }
}

/**
 * Logs debug information to console.
 *
 * @param message Debug message to log
 */
private fun logDebugInfo(message: String) {
    println("🔍 DEBUG: $message")
}

// ==================== STATISTICS CARD COMPONENT ====================

/**
 * Reusable statistics card for dashboard.
 * Shows filtered value when search is active and applicable.
 *
 * @param title Card title
 * @param value Primary value (total)
 * @param modifier Compose modifier
 * @param showFiltered Whether to show filtered value when searching
 * @param searchActive Whether a search is currently active
 * @param filteredValue Value when filtered (search results)
 */
@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    showFiltered: Boolean = false,
    searchActive: Boolean = false,
    filteredValue: String = ""
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (searchActive && showFiltered)
                MaterialTheme.colorScheme.secondary
            else
                MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (searchActive && showFiltered) filteredValue else value,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )

            // Show filtered indicator if applicable
            if (searchActive && showFiltered) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "অনুসন্ধানকৃত",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        }
    }
}

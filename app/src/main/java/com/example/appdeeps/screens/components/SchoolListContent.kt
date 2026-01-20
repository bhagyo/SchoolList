package com.example.appdeeps.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appdeeps.School
import com.example.appdeeps.components.SchoolCard
import com.example.appdeeps.utils.MapUtils

/**
 * SCHOOL LIST CONTENT COMPONENT - Manages school list display states
 *
 * Handles all display states:
 * - Loading state (shows progress indicator)
 * - Empty state (no schools found)
 * - Success state (shows school list)
 */
@Composable
fun SchoolListContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    filteredSchools: List<School>,
    searchQuery: String,
    onSchoolClick: (School) -> Unit,
    onLocationClick: ((School) -> Unit)? = null  // ← FIX: Proper nullable function type
) {
    val context = LocalContext.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
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
                    onLocationClick = onLocationClick ?: { school ->  // ← Already correct
                        MapUtils.openGoogleMaps(school, context)
                    }
                )
            }
        }
    }
}

/**
 * Loading state - shows circular progress indicator
 */
@Composable
private fun LoadingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "ডেটা লোড হচ্ছে \nআপনার ইন্টারনেট সংযোগ পরীক্ষা করুন",
            fontSize = 16.sp
        )
    }
}

/**
 * Empty state - shows message when no schools are found
 */
@Composable
private fun EmptyState(
    searchQuery: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
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
            Text(
                text = "কোনও বিদ্যালয়ের তথ্য পাওয়া যায়নি",
                fontSize = 16.sp
            )
            Text(
                text = "ফায়ারবেসে ডেটা যোগ করুন",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * School list - displays schools in a scrollable column
 */
@Composable
private fun SchoolList(
    schools: List<School>,
    onSchoolClick: (School) -> Unit,
    onLocationClick: (School) -> Unit  // ← This must be non-null
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
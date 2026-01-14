package com.example.appdeeps.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appdeeps.School
import com.example.appdeeps.utils.Calculations

/**
 * STATISTICS DASHBOARD COMPONENT - Shows 4 key metrics cards
 *
 * Features:
 * - 4 cards in 2 rows (2 cards per row)
 * - Shows filtered values when search is active
 * - Color changes for filtered state
 * - Bengali labels and values
 */
@Composable
fun StatisticsDashboard(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    allSchools: List<School>,
    filteredSchools: List<School>,
    isSearching: Boolean
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // First Row: Total Schools & Total Students
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(
                title = "মোট বিদ্যালয়",
                value = if (isLoading) "..." else allSchools.size.toString(),
                modifier = Modifier.weight(1f),
                showFiltered = false,
                searchActive = isSearching,
                filteredValue = filteredSchools.size.toString()
            )

            Spacer(modifier = Modifier.width(12.dp))

            StatCard(
                title = "মোট শিক্ষার্থী",
                value = if (isLoading) "..." else
                    Calculations.calculateTotalStudents(allSchools).toString(),
                modifier = Modifier.weight(1f),
                showFiltered = true,
                searchActive = isSearching,
                filteredValue = Calculations.calculateTotalStudents(filteredSchools).toString()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Second Row: Total Attendance & Average Attendance
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(
                title = "মোট উপস্থিতি",
                value = if (isLoading) "..." else
                    Calculations.calculateTotalAttendance(allSchools).toString(),
                modifier = Modifier.weight(1f),
                showFiltered = true,
                searchActive = isSearching,
                filteredValue = Calculations.calculateTotalAttendance(filteredSchools).toString()
            )

            Spacer(modifier = Modifier.width(12.dp))

            StatCard(
                title = "গড় উপস্থিতি",
                value = if (isLoading) "..." else
                    "${Calculations.calculateAverageAttendance(allSchools)}%",
                modifier = Modifier.weight(1f),
                showFiltered = true,
                searchActive = isSearching,
                filteredValue = "${Calculations.calculateAverageAttendance(filteredSchools)}%"
            )
        }
    }
}

/**
 * INDIVIDUAL STATISTICS CARD
 *
 * Shows a single statistic with optional filtered value
 */
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
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
            // Card Title
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Card Value (shows filtered when searching)
            val displayValue = if (searchActive && showFiltered) filteredValue else value
            Text(
                text = displayValue,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )

            // Filtered indicator (only shows when showing filtered value)
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
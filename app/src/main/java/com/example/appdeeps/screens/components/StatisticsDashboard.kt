package com.example.appdeeps.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appdeeps.School
import com.example.appdeeps.utils.Calculations
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import com.example.appdeeps.R

/**
 * STATISTICS DASHBOARD COMPONENT - Shows 4 key metrics cards in a single row
 *
 * Features:
 * - Compact single row layout (4 cards in one row)
 * - Takes only 25% of screen height
 * - Shows filtered values when search is active
 * - Responsive design for different screen sizes
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
    // Get screen configuration for responsive design
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Responsive card size based on screen width
    val cardHeight = when {
        screenWidth < 360.dp -> 80.dp  // Small phones
        screenWidth < 600.dp -> 90.dp  // Normal phones
        else -> 100.dp                 // Tablets
    }

    val horizontalSpacing = when {
        screenWidth < 360.dp -> 4.dp   // Small phones
        screenWidth < 600.dp -> 6.dp   // Normal phones
        else -> 8.dp                   // Tablets
    }

    val iconSize = when {
        screenWidth < 360.dp -> 16.dp  // Small phones
        screenWidth < 600.dp -> 18.dp  // Normal phones
        else -> 20.dp                  // Tablets
    }

    val valueFontSize = when {
        screenWidth < 360.dp -> 12.sp  // Small phones
        screenWidth < 600.dp -> 14.sp  // Normal phones
        else -> 16.sp                  // Tablets
    }

    val titleFontSize = when {
        screenWidth < 360.dp -> 9.sp   // Small phones
        screenWidth < 600.dp -> 10.sp  // Normal phones
        else -> 11.sp                  // Tablets
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
    ) {
        // 1. Total Schools Card
        StatCard(
            title = "মোট প্রতিষ্ঠান",
            value = if (isLoading) "..." else allSchools.size.toString(),
            filteredValue = filteredSchools.size.toString(),
            showFiltered = true,
            searchActive = isSearching,
            iconRes = R.drawable.ic_school, // Make sure you have this icon
            modifier = Modifier.weight(1f).fillMaxHeight(),
            iconSize = iconSize,
            valueFontSize = valueFontSize,
            titleFontSize = titleFontSize
        )

        // 2. Total Students Card
        StatCard(
            title = "মোট ভোটার",
            value = if (isLoading) "..." else
                Calculations.calculateTotalStudents(allSchools).toString(),
            filteredValue = Calculations.calculateTotalStudents(filteredSchools).toString(),
            showFiltered = true,
            searchActive = isSearching,
            iconRes = R.drawable.ic_student, // Make sure you have this icon
            modifier = Modifier.weight(1f).fillMaxHeight(),
            iconSize = iconSize,
            valueFontSize = valueFontSize,
            titleFontSize = titleFontSize
        )

        // 3. Total Attendance Card
        StatCard(
            title = "মোট উপস্থিতি",
            value = if (isLoading) "..." else
                Calculations.calculateTotalAttendance(allSchools).toString(),
            filteredValue = Calculations.calculateTotalAttendance(filteredSchools).toString(),
            showFiltered = true,
            searchActive = isSearching,
            iconRes = R.drawable.ic_attendance, // Make sure you have this icon
            modifier = Modifier.weight(1f).fillMaxHeight(),
            iconSize = iconSize,
            valueFontSize = valueFontSize,
            titleFontSize = titleFontSize
        )

        // 4. Average Attendance Card
        StatCard(
            title = "গড় উপস্থিতি",
            value = if (isLoading) "..." else
                "${Calculations.calculateAverageAttendance(allSchools)}%",
            filteredValue = "${Calculations.calculateAverageAttendance(filteredSchools)}%",
            showFiltered = true,
            searchActive = isSearching,
            iconRes = R.drawable.ic_average, // Make sure you have this icon
            modifier = Modifier.weight(1f).fillMaxHeight(),
            iconSize = iconSize,
            valueFontSize = valueFontSize,
            titleFontSize = titleFontSize
        )
    }
}

/**
 * INDIVIDUAL STATISTICS CARD - Compact Version
 *
 * Shows a single statistic with icon, value, and title
 */
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    filteredValue: String = "",
    showFiltered: Boolean = false,
    searchActive: Boolean = false,
    iconRes: Int,
    iconSize: androidx.compose.ui.unit.Dp = 18.dp,
    valueFontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    titleFontSize: androidx.compose.ui.unit.TextUnit = 10.sp
) {
    val displayValue = if (searchActive && showFiltered && filteredValue.isNotEmpty()) {
        filteredValue
    } else {
        value
    }

    val isFilteredMode = searchActive && showFiltered && filteredValue.isNotEmpty()

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isFilteredMode)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(all = 6.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(iconSize),
                tint = if (isFilteredMode)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Value
            Text(
                text = displayValue,
                fontSize = valueFontSize,
                fontWeight = FontWeight.Bold,
                color = if (isFilteredMode)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Title
            Text(
                text = title,
                fontSize = titleFontSize,
                fontWeight = FontWeight.Medium,
                color = if (isFilteredMode)
                    MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Filtered indicator (only shows when showing filtered value)
            if (isFilteredMode) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "ফিল্টারকৃত",
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

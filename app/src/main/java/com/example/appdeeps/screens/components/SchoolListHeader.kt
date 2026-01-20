package com.example.appdeeps.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * SCHOOL LIST HEADER COMPONENT - Blue header with title and menu
 *
 * Features:
 * - Blue background (MaterialTheme.primary)
 * - App title in Bengali
 * - Location subtitle
 * - Three-dot menu on the right
 * - Consistent padding and spacing
 */
@Composable
fun SchoolListHeader(
    modifier: Modifier = Modifier,
    onAboutClick: () -> Unit,
    onEmergencyClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title Section (Left side)
            HeaderTitleSection(modifier = Modifier.weight(1f))

            // Menu Section (Right side)
            ThreeDotMenu(
                onAboutClick = onAboutClick,
                onEmergencyClick = onEmergencyClick
            )
        }
    }
}

/**
 * Header title and subtitle section
 */
@Composable
private fun HeaderTitleSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Main Title
        Text(
            text = "উলিপুর ভোটকেন্দ্র পর্যবেক্ষণ",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )

        // Subtitle
        Text(
            text = "কুড়িগ্রাম",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
    }
}
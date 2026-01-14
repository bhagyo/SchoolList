package com.example.appdeeps.screens.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * THREE-DOT MENU - Reusable dropdown menu component
 *
 * This menu shows options when user clicks the three-dot icon
 * Can be used anywhere in the app
 */
@Composable
fun ThreeDotMenu(
    // These are functions that will be called when menu items are clicked
    onAboutClick: () -> Unit,
    onEmergencyClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconColor: Color = Color.White  // Default white for header
) {
    // State to track if menu is open or closed
    var isMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // The three-dot icon button
        IconButton(
            onClick = {
                isMenuExpanded = true  // Open menu when clicked
            }
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu options",
                tint = iconColor
            )
        }

        // The dropdown menu (only shown when isMenuExpanded = true)
        SchoolListDropdownMenu(
            expanded = isMenuExpanded,
            onDismiss = { isMenuExpanded = false },  // Close menu
            onAboutClick = onAboutClick,
            onEmergencyClick = onEmergencyClick
        )
    }
}

/**
 * DROPDOWN MENU CONTENT - The actual menu items
 *
 * Separated so we can test it independently
 */
@Composable
fun SchoolListDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onAboutClick: () -> Unit,
    onEmergencyClick: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        // "About Us" menu item
        DropdownMenuItem(
            text = {
                Text("আমাদের সম্পর্কে")
            },
            onClick = {
                onDismiss()  // Close menu first
                onAboutClick()  // Then show About dialog
            }
        )

        // "Emergency Numbers" menu item
        DropdownMenuItem(
            text = {
                Text("জরুরী নাম্বার")
            },
            onClick = {
                onDismiss()  // Close menu first
                onEmergencyClick()  // Then show Emergency dialog
            }
        )
    }
}
package com.example.appdeeps.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun SchoolSearchBar(
    modifier: Modifier = Modifier,
    searchText: TextFieldValue,
    onSearchTextChange: (TextFieldValue) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        SearchBarContent(
            searchText = searchText,
            onSearchTextChange = onSearchTextChange
        )
    }
}

@Composable
private fun SearchBarContent(
    searchText: TextFieldValue,
    onSearchTextChange: (TextFieldValue) -> Unit
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

        // ✅ FIX: weight applied to Box, NOT TextField
        Box(
            modifier = Modifier.weight(1f)
        ) {
            SearchTextField(
                searchText = searchText,
                onSearchTextChange = onSearchTextChange
            )
        }

        if (searchText.text.isNotEmpty()) {
            ClearSearchButton {
                onSearchTextChange(TextFieldValue(""))
            }
        }
    }
}

@Composable
private fun SearchTextField(
    searchText: TextFieldValue,
    onSearchTextChange: (TextFieldValue) -> Unit
) {
    TextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "কেন্দ্রের নাম, নম্বর বা ইউনিয়ন",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            disabledContainerColor = MaterialTheme.colorScheme.background,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
private fun ClearSearchButton(
    onClearClick: () -> Unit
) {
    IconButton(onClick = onClearClick) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Clear search",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

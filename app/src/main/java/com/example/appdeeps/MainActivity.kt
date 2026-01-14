package com.example.appdeeps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.appdeeps.screens.SchoolDetailsScreen
import com.example.appdeeps.screens.SchoolListScreen
import com.example.appdeeps.ui.theme.AppDeepSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppDeepSTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    var selectedSchool by remember { mutableStateOf<School?>(null) }

    if (selectedSchool != null) {
        SchoolDetailsScreen(
            school = selectedSchool!!,
            onBackClick = { selectedSchool = null }
        )
    } else {
        SchoolListScreen(
            onSchoolClick = { school ->
                selectedSchool = school
            }
        )
    }
}
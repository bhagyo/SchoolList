package com.example.appdeeps.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appdeeps.School
import com.example.appdeeps.components.TeacherCard
import com.example.appdeeps.components.InfoCard
import com.example.appdeeps.components.InfoText
import com.example.appdeeps.utils.openSchoolInGoogleMaps
import com.example.appdeeps.utils.shareSchoolInfo

@Composable
fun SchoolDetailsScreen(
    school: School,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        /* ---------------- HEADER ---------------- */
        Header(school = school, onBackClick = onBackClick)

        /* ---------------- CONTENT ---------------- */
        Column(modifier = Modifier.padding(16.dp)) {

            /* -------- STATUS CARD -------- */
            StatusCard(school = school)

            Spacer(modifier = Modifier.height(16.dp))

            /* -------- BASIC INFO -------- */
            BasicInfoCard(school = school)

            Spacer(modifier = Modifier.height(16.dp))

            /* -------- HEAD TEACHER -------- */
            TeacherCard(
                title = "প্রিজাইডিং অফিসার",
                name = school.headmasterName,
                phone = school.headmasterMobile,
                context = context
            )

            Spacer(modifier = Modifier.height(16.dp))

            /* -------- ASSISTANT HEAD -------- */
            TeacherCard(
                title = "সহকারী প্রিজাইডিং অফিসার",
                name = school.asstHeadmasterName,
                phone = school.asstHeadmasterMobile,
                context = context
            )

            Spacer(modifier = Modifier.height(16.dp))

            /* -------- POLICE OFFICER (Conditional) -------- */
            // Only show if police name is not empty
            if (school.policeName.isNotEmpty() || school.policeMobile.isNotEmpty()) {
                TeacherCard(
                    title = "নিয়োজিত পুলিশ কর্মকর্তা",
                    name = if (school.policeName.isNotEmpty()) school.policeName else "তথ্য নেই",
                    phone = if (school.policeMobile.isNotEmpty()) school.policeMobile else "তথ্য নেই",
                    context = context
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            /* -------- ACTION BUTTONS -------- */
            ActionButtons(context = context, school = school)
        }
    }
}

/* ---------------- SUB-COMPONENTS ---------------- */

@Composable
private fun Header(school: School, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = school.schoolName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "বিদ্যালয় নং: ${school.schoolNumber}",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun StatusCard(school: School) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (school.schoolStatus) {
                "good" -> Color(0xFF4CAF50)
                "normal" -> Color(0xFF2196F3)
                else -> Color(0xFFF44336)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                Text(
                    text = when (school.schoolStatus) {
                        "good" -> "ভাল"
                        "normal" -> "মধ্যম"
                        else -> "সহায়তা প্রয়োজন"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "ভোটকেন্দ্র অবস্থা",
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "${school.attendancePercentage}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = when {
                        school.attendancePercentage >= 60 -> Color(0xFF4CAF50)
                        school.attendancePercentage >= 40 -> Color(0xFF2196F3)
                        else -> Color(0xFFF44336)
                    }
                )
            }
        }
    }
}

@Composable
private fun BasicInfoCard(school: School) {
    InfoCard(title = "মৌলিক তথ্য") {
        InfoText("ইউনিয়ন", school.unionName)
        InfoText("ঠিকানা", school.address)
        InfoText("পুরুষ ভোটার", "${school.maleStudents} জন")
        InfoText("মহিলা ভোটার", "${school.femaleStudents} জন")
        InfoText("মোট ভোটার", "${school.totalStudents} জন")
        InfoText("প্রাপ্ত ভোট", "${school.dailyAttendance} জন")
    }
}

@Composable
private fun ActionButtons(context: Context, school: School) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Map Button
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                openSchoolInGoogleMaps(
                    context,
                    school.latitude,
                    school.longitude,
                    school.schoolName
                )
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("গুগল ম্যাপে অবস্থান দেখুন")
        }

        // Share Button
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                shareSchoolInfo(context, school)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("তথ্য শেয়ার করুন")
        }
    }
}
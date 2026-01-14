package com.example.appdeeps.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.appdeeps.School

// Function to open Google Maps
fun openSchoolInGoogleMaps(
    context: Context,
    latitude: Double,
    longitude: Double,
    name: String
) {
    if (latitude == 0.0 && longitude == 0.0) return

    val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(name)})")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.setPackage("com.google.android.apps.maps")

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
            )
        )
    }
}

// Function to share school information
fun shareSchoolInfo(context: Context, school: School) {
    val shareText = """
        📚 বিদ্যালয়: ${school.schoolName}
        🔢 নং: ${school.schoolNumber}
        🏘️ ইউনিয়ন: ${school.unionName}
        
        👨‍🏫 প্রধান শিক্ষক: ${school.headmasterName}
        📞 মোবাইল: ${school.headmasterMobile}
        
        👨‍🏫 সহকারী প্রধান: ${school.asstHeadmasterName}
        📞 মোবাইল: ${school.asstHeadmasterMobile}
        
        📊 উপস্থিতি: ${school.attendancePercentage}%
        👦 ছাত্র: ${school.maleStudents} জন
        👧 ছাত্রী: ${school.femaleStudents} জন
        👨‍🎓 মোট ছাত্র-ছাত্রী: ${school.totalStudents} জন
        
        📍 অবস্থান: https://maps.google.com/?q=${school.latitude},${school.longitude}
        
        #উলিপুর_বিদ্যালয়_মনিটরিং
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "${school.schoolName} - বিদ্যালয় তথ্য")
    }

    context.startActivity(Intent.createChooser(intent, "তথ্য শেয়ার করুন"))
}
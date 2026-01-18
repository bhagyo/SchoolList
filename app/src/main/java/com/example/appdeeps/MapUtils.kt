package com.example.appdeeps

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

fun openSchoolInGoogleMaps(context: Context, latitude: Double, longitude: Double, schoolName: String) {
    // Check if coordinates are valid (not 0,0)
    if (latitude == 0.0 && longitude == 0.0) {
        println("❌ Invalid coordinates for $schoolName")
        return
    }

    // Create Google Maps URI
    val gmmIntentUri =
        "geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(schoolName)})".toUri()
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")

    // Try to open Google Maps
    try {
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        // If Google Maps is not installed, open in browser
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude".toUri()
        )
        context.startActivity(webIntent)
    }
}
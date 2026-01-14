package com.example.appdeeps.utils

import android.content.Intent
import android.net.Uri
import com.example.appdeeps.School

/**
 * MAP UTILITIES - Handles Google Maps integration
 */
object MapUtils {

    /**
     * Opens Google Maps for a school location
     * Falls back to browser if Google Maps is not installed
     */
    fun openGoogleMaps(school: School, context: android.content.Context) {
        if (school.latitude != 0.0 && school.longitude != 0.0) {
            try {
                // Try to open in Google Maps app
                val gmmIntentUri = Uri.parse(
                    "geo:${school.latitude},${school.longitude}?q=${school.latitude},${school.longitude}(${Uri.encode(school.schoolName)})"
                )
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                context.startActivity(mapIntent)
            } catch (e: Exception) {
                // Fallback to browser
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=${school.latitude},${school.longitude}")
                )
                context.startActivity(webIntent)
            }
        }
    }
}
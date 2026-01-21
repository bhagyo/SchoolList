package com.example.appdeeps.utils

import com.google.firebase.database.DataSnapshot

object FirebaseTypeAdapter {
    fun getString(snapshot: DataSnapshot, key: String): String {
        return when (val value = snapshot.child(key).value) {
            is String -> value
            is Long -> value.toString()
            is Int -> value.toString()
            else -> value?.toString() ?: ""
        }
    }

    fun getInt(snapshot: DataSnapshot, key: String): Int {
        return when (val value = snapshot.child(key).value) {
            is Int -> value
            is Long -> value.toInt()
            is String -> value.toIntOrNull() ?: 0
            else -> 0
        }
    }

    fun getDouble(snapshot: DataSnapshot, key: String): Double {
        return when (val value = snapshot.child(key).value) {
            is Double -> value
            is Long -> value.toDouble()
            is Int -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }
}
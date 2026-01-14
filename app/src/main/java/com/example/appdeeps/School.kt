package com.example.appdeeps

data class School(
    // Basic Information
    val id: String = "",
    val schoolNumber: String = "",
    val schoolName: String = "",
    val schoolStatus: String = "", // "good", "normal", "bad"

    // Location Information
    val unionName: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    // Student Statistics
    val maleStudents: Int = 0,
    val femaleStudents: Int = 0,
    val totalStudents: Int = 0,
    val dailyAttendance: Int = 0,

    // Head Teacher Information
    val headmasterName: String = "",
    val headmasterMobile: String = "",
    val asstHeadmasterName: String = "",
    val asstHeadmasterMobile: String = "",

    // Police Officer Information
    val policeName: String = "",
    val policeMobile: String = "",

    // Timestamp
    val lastUpdated: String = ""
) {
    // Computed property to calculate attendance percentage
    val attendancePercentage: Int
        get() = if (totalStudents > 0) {
            ((dailyAttendance.toDouble() / totalStudents.toDouble()) * 100).toInt()
        } else {
            0
        }

    // Empty constructor for Firebase
    constructor() : this(
        id = "",
        schoolNumber = "",
        schoolName = "",
        schoolStatus = "",
        unionName = "",
        address = "",
        latitude = 0.0,
        longitude = 0.0,
        maleStudents = 0,
        femaleStudents = 0,
        totalStudents = 0,
        dailyAttendance = 0,
        headmasterName = "",
        headmasterMobile = "",
        asstHeadmasterName = "",
        asstHeadmasterMobile = "",
        policeName = "",
        policeMobile = "",
        lastUpdated = ""
    )
}
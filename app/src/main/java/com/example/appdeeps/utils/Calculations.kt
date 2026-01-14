package com.example.appdeeps.utils

import com.example.appdeeps.School

/**
 * CALCULATIONS UTILITY - Contains helper functions for school statistics
 */
object Calculations {

    /**
     * Calculates total number of students across all schools
     */
    fun calculateTotalStudents(schools: List<School>): Int {
        return schools.sumOf { it.totalStudents }
    }

    /**
     * Calculates total daily attendance across all schools
     */
    fun calculateTotalAttendance(schools: List<School>): Int {
        return schools.sumOf { it.dailyAttendance }
    }

    /**
     * Calculates average attendance percentage across schools
     * Returns 0 if the list is empty
     */
    fun calculateAverageAttendance(schools: List<School>): Int {
        if (schools.isEmpty()) return 0
        val total = schools.sumOf { it.attendancePercentage }
        return total / schools.size
    }
}
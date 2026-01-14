package com.example.appdeeps

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object FirebaseManager {
    // Your database URL (update if needed)
    private val database = Firebase.database("https://ulipur-school-monitor-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val schoolsRef = database.getReference("schools")

    // Function to save sample schools (optional - since you manually added)
    fun saveSampleSchools() {
        println("💾 FirebaseManager: Saving sample schools...")

        val sampleSchools = listOf(
            School(
                id = "001",
                schoolNumber = "০১",
                schoolName = "উলিপুর মডেল সরকারি প্রাথমিক বিদ্যালয়",
                schoolStatus = "good",
                unionName = "উলিপুর সদর",
                address = "উলিপুর বাজার সংলগ্ন",
                maleStudents = 215,
                femaleStudents = 189,
                totalStudents = 404,
                dailyAttendance = 380,
                headmasterName = "মোঃ আব্দুল হামিদ",
                headmasterMobile = "০১৭১২৩৪৫৬৭৮",
                asstHeadmasterName = "মোঃ জাহাঙ্গীর আলম",
                asstHeadmasterMobile = "০১৮১২৩৪৫৬৭৮",
                latitude = 25.7743,
                longitude = 89.6441,
                policeName = "মোঃ পুলিশ অফিসার",
                policeMobile = "০১৯১২৩৪৫৬৭৮",
                lastUpdated = "২০২৪-০১-১২"
            ),
            School(
                id = "002",
                schoolNumber = "০২",
                schoolName = "বন্দবিল সরকারি প্রাথমিক বিদ্যালয়",
                schoolStatus = "bad",
                unionName = "বন্দবিল",
                address = "বন্দবিল বাজার",
                maleStudents = 120,
                femaleStudents = 110,
                totalStudents = 230,
                dailyAttendance = 150,
                headmasterName = "মোঃ রফিকুল ইসলাম",
                headmasterMobile = "০১৭৮৭৬৫৪৩২১",
                asstHeadmasterName = "মোসাঃ সেলিনা আক্তার",
                asstHeadmasterMobile = "০১৮৭৬৫৪৩২১০",
                latitude = 25.7890,
                longitude = 89.6321,
                policeName = "মোঃ পুলিশ কর্মকর্তা",
                policeMobile = "০১৯৮৭৬৫৪৩২১",
                lastUpdated = "২০২৪-০১-১২"
            )
        )

        sampleSchools.forEach { school ->
            println("💾 Saving: ${school.schoolName}")
            schoolsRef.child(school.id).setValue(school)
                .addOnSuccessListener {
                    println("✅ Saved: ${school.schoolName}")
                }
                .addOnFailureListener { e ->
                    println("❌ Failed: ${e.message}")
                }
        }
    }

    // Function to get all schools (for future use)
    fun getAllSchools(callback: (List<School>) -> Unit) {
        schoolsRef.get().addOnSuccessListener { snapshot ->
            val schoolList = mutableListOf<School>()

            for (child in snapshot.children) {
                val school = child.getValue(School::class.java)
                school?.let { schoolList.add(it) }
            }

            callback(schoolList)
        }.addOnFailureListener { e ->
            println("❌ Error getting schools: ${e.message}")
            callback(emptyList())
        }
    }
}
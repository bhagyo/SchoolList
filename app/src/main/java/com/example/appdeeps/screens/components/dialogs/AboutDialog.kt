package com.example.appdeeps.screens.components.dialogs

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

/**
 * This dialog appears when user clicks "About" in the three-dot menu
 */
@Composable
fun AboutDialog(
    onDismiss: () -> Unit  // Function to close the dialog
) {
    AlertDialog(
        onDismissRequest = onDismiss,  // Close when clicked outside
        title = {
            Text(
                text = "আমাদের সম্পর্কে",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = """
                    এই অ্যাপটি উলিপুর উপজেলার ভোটকেন্দ্র  মনিটরিং সিস্টেমের জন্য তৈরি করা হয়েছে।
                    
                    উদ্দেশ্য:
                    • সকল বিদ্যালয়ের উপস্থিতি মনিটরিং
                    • শিক্ষকদের সাথে সহজ যোগাযোগ
                    • বিদ্যালয়ের অবস্থান দেখা
                    • জরুরী ক্ষেত্রে দ্রুত সাহায্য
                    
                    সংস্করণ: ১.০.০
                    ডেভেলপার: উলিপুর ডিজিটাল টিম
                """.trimIndent()
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("ঠিক আছে")
            }
        }
    )
}
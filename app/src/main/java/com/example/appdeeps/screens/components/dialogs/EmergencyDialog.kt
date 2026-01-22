package com.example.appdeeps.screens.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * This dialog appears when user clicks "Emergency Numbers" in the menu
 */
@Composable
fun EmergencyDialog(
    onDismiss: () -> Unit  // Function to close the dialog
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "জরুরী নাম্বার",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "জরুরী যোগাযোগের জন্য:",
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // National emergency services
                Text("• জাতীয় জরুরী সেবা: ৯৯৯")
                Text("• থানা: ০১৭１３-XXX")
                Text("• ফায়ার সার্ভিস: ０１７１-XXX")
                Text("• এম্বুলেন্স: ০১৭０-XXX")

                Spacer(modifier = Modifier.height(8.dp))

                // Ulipur specific contacts
                Text("উলিপুর উপজেলা:")
                Text("• উপজেলা নির্বাহী অফিসার: XXX")
                Text("• শিক্ষা অফিসার: XXX")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("ঠিক আছে")
            }
        }
    )
}
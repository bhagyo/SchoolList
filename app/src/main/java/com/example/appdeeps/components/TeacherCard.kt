package com.example.appdeeps.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TeacherCard(
    title: String,
    name: String,
    phone: String,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "• নাম: $name", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "• মোবাইল: $phone",
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )

                if (phone.isNotBlank()) {

                    IconButton(onClick = { makePhoneCall(context, phone) }) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = "Call",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = { openWhatsApp(context, phone) }) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = "WhatsApp",
                            tint = Color(0xFF25D366)
                        )
                    }
                }
            }
        }
    }
}

// Helper functions for TeacherCard
private fun makePhoneCall(context: Context, phone: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
    context.startActivity(intent)
}

private fun openWhatsApp(context: Context, phone: String) {
    val number = phone.replace("+", "").replace(" ", "")
    val uri = Uri.parse("https://wa.me/$number")
    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
}
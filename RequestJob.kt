package com.hanif.kajlagbe

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RequestJob(navController: NavController, workerId: String) {

    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val context = LocalContext.current

    var location by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val requestId = "${userId}_${workerId}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Request Job", fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading,
            onClick = {

                if (location.isBlank() || note.isBlank()) {
                    Toast.makeText(context, "All fields required", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                loading = true

                val requestData = RequestModel(
                    requestId = requestId,
                    userId = userId,
                    workerId = workerId,
                    location = location,
                    note = note,
                    status = "pending",
                    timestamp = System.currentTimeMillis()
                )

                firestore.collection("jobRequests")
                    .document(requestId)
                    .set(requestData)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Request Sent ✅", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                        loading = false
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        loading = false
                    }
            }
        ) {
            Text("Send Request")
        }
    }
}

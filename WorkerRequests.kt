package com.hanif.kajlagbe

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun WorkerRequests(navController: NavController) {

    val firestore = FirebaseFirestore.getInstance()
    val workerId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var requests by remember { mutableStateOf<List<RequestModel>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        firestore.collection("jobRequests")
            .whereEqualTo("workerId", workerId)
            .addSnapshotListener { snapshot, _ ->

                if (snapshot != null) {
                    requests = snapshot.documents.map { doc ->
                        RequestModel(
                            requestId = doc.id,
                            userId = doc.getString("userId") ?: "",
                            workerId = workerId,
                            location = doc.getString("location") ?: "",
                            note = doc.getString("note") ?: "",
                            status = doc.getString("status") ?: "pending",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }.sortedByDescending { it.timestamp }

                    loading = false
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            stringResource(R.string.worker_requests),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        if (requests.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_requests))
            }
            return
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(requests) { request ->
                WorkerRequestCard(request, navController)
            }
        }
    }
}

@Composable
fun WorkerRequestCard(
    request: RequestModel,
    navController: NavController
) {

    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var processing by remember { mutableStateOf(false) }

    val chatId = "${request.userId}_${request.workerId}"
    
    // Pre-fetch strings for use inside non-composable lambdas
    val acceptedMsg = stringResource(R.string.accepted_chat_ready)
    val rejectedMsg = stringResource(R.string.rejected)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {

        Column(modifier = Modifier.padding(14.dp)) {

            Text("📍 ${stringResource(R.string.location)}: ${request.location}", fontWeight = FontWeight.Bold)
            Text("📝 Note: ${request.note}")

            Spacer(Modifier.height(8.dp))

            Text(
                "Status: ${request.status.uppercase()}",
                fontWeight = FontWeight.Bold,
                color = when (request.status) {
                    "pending" -> Color(0xFFFF8C00)
                    "accepted" -> Color(0xFF2E7D32)
                    "rejected" -> Color.Red
                    else -> Color.Gray
                }
            )

            Spacer(Modifier.height(14.dp))

            /* ✅ ACCEPT + CREATE CHAT */
            if (request.status == "pending") {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Button(
                        enabled = !processing,
                        onClick = {

                            processing = true

                            firestore.collection("jobRequests")
                                .document(request.requestId)
                                .update("status", "accepted")
                                .addOnSuccessListener {

                                    /* ✅ CREATE CHAT ONLY ONCE */
                                    val chatData = mapOf(
                                        "chatId" to chatId,
                                        "userId" to request.userId,
                                        "workerId" to request.workerId,
                                        "lastMessage" to "",
                                        "lastTime" to System.currentTimeMillis(),
                                        "userDeleted" to false,
                                        "workerDeleted" to false
                                    )

                                    firestore.collection("chats")
                                        .document(chatId)
                                        .set(chatData)

                                    Toast.makeText(
                                        context,
                                        acceptedMsg,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    processing = false
                                }
                        }
                    ) {
                        Text(stringResource(R.string.accept))
                    }

                    Button(
                        enabled = !processing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        onClick = {

                            processing = true

                            firestore.collection("jobRequests")
                                .document(request.requestId)
                                .update("status", "rejected")
                                .addOnSuccessListener {

                                    Toast.makeText(
                                        context,
                                        rejectedMsg,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    processing = false
                                }
                        }
                    ) {
                        Text(stringResource(R.string.reject))
                    }
                }
            }

            /* ✅ OPEN CHAT AFTER ACCEPT */
            if (request.status == "accepted") {

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController.navigate("chat/$chatId")
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.open_chat))
                }
            }
        }
    }
}

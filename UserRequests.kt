package com.hanif.kajlagbe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun UserRequests(navController: NavController) {

    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var requests by remember { mutableStateOf<List<RequestModel>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {

        firestore.collection("jobRequests")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->

                if (snapshot != null) {

                    requests = snapshot.documents.map { doc ->
                        RequestModel(
                            requestId = doc.id,
                            userId = doc.getString("userId") ?: "",
                            workerId = doc.getString("workerId") ?: "",
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

        Text(stringResource(R.string.my_requests), fontSize = 22.sp, fontWeight = FontWeight.Bold)

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

                val chatId = "${request.userId}_${request.workerId}"

                Card(modifier = Modifier.fillMaxWidth()) {

                    Column(modifier = Modifier.padding(14.dp)) {

                        Text("📍 ${request.location}", fontWeight = FontWeight.Bold)
                        Text("📝 ${request.note}")

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "${stringResource(R.string.status)}: ${request.status.uppercase()}",
                            fontWeight = FontWeight.Bold,
                            color = when (request.status) {
                                "pending" -> Color(0xFFFF8C00)
                                "accepted" -> Color(0xFF2E7D32)
                                "rejected" -> Color.Red
                                else -> Color.Gray
                            }
                        )

                        if (request.status == "accepted") {

                            Spacer(Modifier.height(12.dp))

                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navController.navigate("chat/$chatId")
                                }
                            ) {
                                Icon(Icons.Default.Chat, null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.open_chat))
                            }
                        }
                    }
                }
            }
        }
    }
}

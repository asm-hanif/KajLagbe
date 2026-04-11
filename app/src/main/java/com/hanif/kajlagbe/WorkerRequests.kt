package com.hanif.kajlagbe

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerRequests(navController: NavController) {

    val firestore = FirebaseFirestore.getInstance()
    val workerId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var requests by remember { mutableStateOf<List<JobRequest>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        firestore.collection("jobRequests")
            .whereEqualTo("workerId", workerId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    requests = snapshot.toObjects(JobRequest::class.java)
                        .sortedByDescending { it.timestamp }
                    loading = false
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.works), 
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    ) 
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (requests.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.WorkOff, 
                        contentDescription = null, 
                        modifier = Modifier.size(80.dp), 
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_requests),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(requests) { request ->
                        WorkerRequestCard(request, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkerRequestCard(
    request: JobRequest,
    navController: NavController
) {
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var processing by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    val chatId = "${request.userId}_${request.workerId}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Client Name and Status
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween, 
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.userName,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusBadge(request.status)
            }
            
            Spacer(Modifier.height(4.dp))
            Text(
                text = request.scheduledDate, 
                fontSize = 12.sp, 
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Medium
            )

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Details Section
            InfoRow(Icons.Default.LocationOn, request.location)
            Spacer(Modifier.height(8.dp))
            InfoRow(Icons.Default.Notes, request.note)

            Spacer(Modifier.height(16.dp))

            // Actions Section
            if (request.status == "pending") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = !processing,
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            processing = true
                            firestore.runTransaction { transaction ->
                                val requestRef = firestore.collection("jobRequests").document(request.requestId)
                                val workerRef = firestore.collection("workers").document(request.workerId)
                                
                                transaction.update(requestRef, "status", "accepted")
                                transaction.update(workerRef, "isBusy", true)
                                
                                val chatData = mapOf(
                                    "chatId" to chatId,
                                    "userId" to request.userId,
                                    "workerId" to request.workerId,
                                    "lastMessage" to "Job Accepted",
                                    "lastTime" to System.currentTimeMillis(),
                                    "userDeleted" to false,
                                    "workerDeleted" to false
                                )
                                transaction.set(firestore.collection("chats").document(chatId), chatData)
                                
                                null
                            }.addOnSuccessListener {
                                Toast.makeText(context, context.getString(R.string.accepted_chat_ready), Toast.LENGTH_SHORT).show()
                                processing = false
                            }.addOnFailureListener {
                                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                processing = false
                            }
                        }
                    ) {
                        if (processing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.accept))
                        }
                    }

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = !processing,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)),
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            processing = true
                            firestore.collection("jobRequests")
                                .document(request.requestId)
                                .update("status", "rejected")
                                .addOnSuccessListener {
                                    Toast.makeText(context, context.getString(R.string.rejected), Toast.LENGTH_SHORT).show()
                                    processing = false
                                }
                        }
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.reject))
                    }
                }
            }

            if (request.status == "accepted") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate("chat/$chatId") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.open_chat))
                    }
                    
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !processing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)),
                        onClick = { showCancelDialog = true }
                    ) {
                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cancel Job")
                    }
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel this job?") },
            text = { Text("Are you sure you want to cancel this accepted job? This will notify the hirer and mark the job as cancelled.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        processing = true
                        
                        firestore.collection("jobRequests")
                            .whereEqualTo("workerId", request.workerId)
                            .whereEqualTo("status", "accepted")
                            .get()
                            .addOnSuccessListener { snapshot ->
                                val otherAccepted = snapshot.documents.any { it.id != request.requestId }
                                
                                firestore.runTransaction { transaction ->
                                    val requestRef = firestore.collection("jobRequests").document(request.requestId)
                                    val workerRef = firestore.collection("workers").document(request.workerId)
                                    
                                    transaction.update(requestRef, "status", "cancelled")
                                    transaction.update(workerRef, "isBusy", otherAccepted)
                                    null
                                }.addOnSuccessListener {
                                    Toast.makeText(context, "Job Cancelled ✅", Toast.LENGTH_SHORT).show()
                                    processing = false
                                }.addOnFailureListener {
                                    Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                    processing = false
                                }
                            }
                    }
                ) {
                    Text("Yes, Cancel", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Back")
                }
            }
        )
    }
}

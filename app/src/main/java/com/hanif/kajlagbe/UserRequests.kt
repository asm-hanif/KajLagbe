package com.hanif.kajlagbe

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRequests(navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val userId = user.uid
    val context = LocalContext.current

    var requests by remember { mutableStateOf<List<JobRequest>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var realUserName by remember { mutableStateOf(user.displayName ?: "User") }

    LaunchedEffect(userId) {
        firestore.collection("users").document(userId).get().addOnSuccessListener { doc ->
            val name = doc.getString("name")
            if (name != null && name != "User") realUserName = name
        }

        firestore.collection("jobRequests")
            .whereEqualTo("userId", userId)
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
                    Text(stringResource(R.string.my_requests), fontWeight = FontWeight.Black, fontSize = 24.sp) 
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (requests.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.no_requests), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(16.dp)) {
                    items(requests) { request ->
                        RequestCard(request, navController, firestore, userId, realUserName, context)
                    }
                }
            }
        }
    }
}

@Composable
fun RequestCard(
    request: JobRequest,
    navController: NavController,
    firestore: FirebaseFirestore,
    userId: String,
    userName: String,
    context: android.content.Context
) {
    var showRatingDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var workerContact by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    val chatId = "${request.userId}_${request.workerId}"

    LaunchedEffect(request.status) {
        if (request.status == "accepted") {
            firestore.collection("workers").document(request.workerId).get().addOnSuccessListener { doc ->
                workerContact = doc.getString("contact")
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = request.workerName, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { navController.navigate("${Routes.WORKER_DETAILS}/${request.workerId}") })
                StatusBadge(request.status)
            }
            
            Text(text = request.scheduledDate, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium)
            Divider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
            InfoRow(Icons.Default.Work, request.workType)
            Spacer(Modifier.height(8.dp))
            InfoRow(Icons.Default.LocationOn, request.location)
            Spacer(Modifier.height(8.dp))
            InfoRow(Icons.Default.Notes, request.note)

            if (request.status == "accepted" && workerContact != null) {
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)).clickable {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$workerContact"))
                    context.startActivity(intent)
                }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Call, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Worker Contact", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(workerContact!!, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (request.status == "pending") {
                OutlinedButton(onClick = { showCancelDialog = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Cancel Request")
                }
            } else if (request.status == "accepted") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(modifier = Modifier.weight(1f), onClick = { navController.navigate("chat/$chatId") }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary), shape = RoundedCornerShape(12.dp)) {
                            Text("Chat")
                        }
                        Button(modifier = Modifier.weight(1f), enabled = !isProcessing, onClick = { showCompleteDialog = true }, shape = RoundedCornerShape(12.dp)) {
                            Text("Complete")
                        }
                    }
                }
            } else if (request.status == "completed") {
                Button(onClick = { showRatingDialog = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))) {
                    Text("Rate Experience", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Mark as Completed?") },
            confirmButton = {
                TextButton(onClick = {
                    showCompleteDialog = false
                    isProcessing = true
                    firestore.runTransaction { transaction ->
                        transaction.update(firestore.collection("jobRequests").document(request.requestId), "status", "completed")
                        transaction.update(firestore.collection("workers").document(request.workerId), "isBusy", false)
                        null
                    }.addOnSuccessListener {
                        isProcessing = false
                        showRatingDialog = true
                    }.addOnFailureListener { e ->
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        isProcessing = false
                    }
                }) { Text("Yes") }
            }, dismissButton = { TextButton(onClick = { showCompleteDialog = false }) { Text("No") } }
        )
    }

    if (showRatingDialog) {
        var rating by remember { mutableIntStateOf(5) }
        var comment by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }
        val reviewId = request.requestId 

        LaunchedEffect(Unit) {
            firestore.collection("reviews").document(reviewId).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    rating = doc.getLong("rating")?.toInt() ?: 5
                    comment = doc.getString("comment") ?: ""
                }
            }
        }

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showRatingDialog = false },
            title = { Text("Rate ${request.workerName}") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row {
                        (1..5).forEach { index ->
                            IconButton(onClick = { rating = index }) {
                                Icon(if (index <= rating) Icons.Default.Star else Icons.Default.StarOutline, null, tint = Color(0xFFFFB300), modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                    OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("Comment") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    isSubmitting = true
                    val review = hashMapOf(
                        "reviewId" to reviewId,
                        "workerId" to request.workerId,
                        "userId" to userId,
                        "userName" to userName,
                        "rating" to rating,
                        "comment" to comment,
                        "timestamp" to Timestamp.now()
                    )

                    // JUST SAVE. Background Cloud Function will do the math.
                    firestore.collection("reviews").document(reviewId)
                        .set(review)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Review saved! ✅", Toast.LENGTH_SHORT).show()
                            showRatingDialog = false
                            isSubmitting = false
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            isSubmitting = false
                        }
                }) { Text("Submit") }
            }
        )
    }
}

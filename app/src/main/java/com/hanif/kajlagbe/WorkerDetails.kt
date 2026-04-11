package com.hanif.kajlagbe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDetails(workerId: String, navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    
    var worker by remember { mutableStateOf<Worker?>(null) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isBusyLive by remember { mutableStateOf(false) }
    
    var workerLoaded by remember { mutableStateOf(false) }
    var reviewsLoaded by remember { mutableStateOf(false) }
    var requestsLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(workerId) {
        // 1. Fetch worker details live
        firestore.collection("workers").document(workerId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                worker = snapshot.toObject(Worker::class.java)?.copy(uid = snapshot.id)
            }
            workerLoaded = true
        }

        // 2. Fetch reviews live (Job History)
        firestore.collection("reviews")
            .whereEqualTo("workerId", workerId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val allReviews = snapshot.toObjects(Review::class.java)
                    // Deduplicate reviews by userId just in case
                    reviews = allReviews
                        .sortedByDescending { it.timestamp }
                        .distinctBy { it.userId }
                }
                reviewsLoaded = true
            }

        // 3. Check if busy live (by checking for accepted requests)
        firestore.collection("jobRequests")
            .whereEqualTo("workerId", workerId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    isBusyLive = snapshot.documents.any { it.getString("status") == "accepted" }
                }
                requestsLoaded = true
            }
    }

    /* ✅ SELF-HEALING LOGIC: Sync worker document if stats or busy state are wrong */
    LaunchedEffect(workerLoaded, reviewsLoaded, requestsLoaded) {
        if (workerLoaded && reviewsLoaded && requestsLoaded) {
            val w = worker ?: return@LaunchedEffect
            
            val actualReviewCount = reviews.size
            val actualRating = if (actualReviewCount > 0) reviews.sumOf { it.rating.toDouble() } / actualReviewCount else 0.0
            
            // Check if Firestore document needs updating
            val needsUpdate = w.reviewCount != actualReviewCount || 
                              abs(w.rating - actualRating) > 0.01 ||
                              w.isBusy != isBusyLive

            if (needsUpdate) {
                firestore.collection("workers").document(workerId).update(
                    "reviewCount", actualReviewCount,
                    "rating", actualRating.toFloat(),
                    "isBusy", isBusyLive
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.worker_details)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            worker?.let { w ->
                if (w.uid != currentUid) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp
                    ) {
                        Button(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            onClick = { navController.navigate("${Routes.REQUEST_JOB}/${w.uid}") },
                            enabled = w.isAvailable,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (w.isAvailable) stringResource(R.string.request_job) else "Unavailable")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (!workerLoaded) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (worker == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Worker not found")
            }
        } else {
            val w = worker!!
            
            // UI logic: Prefer live calculated values for accuracy while sync happens
            val displayIsBusy = if (requestsLoaded) isBusyLive else w.isBusy
            val displayReviewCount = if (reviewsLoaded) reviews.size else w.reviewCount
            val displayRating = if (reviewsLoaded) {
                if (reviews.isNotEmpty()) "%.1f".format(reviews.sumOf { it.rating.toDouble() } / reviews.size) else "0.0"
            } else {
                "%.1f".format(w.rating)
            }

            val statusColor = when {
                !w.isAvailable -> Color(0xFFD32F2F)
                displayIsBusy -> Color(0xFFFF9800)
                else -> Color(0xFF4CAF50)
            }
            val statusText = when {
                !w.isAvailable -> "Unavailable"
                displayIsBusy -> "Busy"
                else -> "Available"
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(w.name, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                                Surface(color = statusColor, shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        statusText,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Work, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(w.workType, style = MaterialTheme.typography.bodyLarge)
                            }
                            
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(w.location, style = MaterialTheme.typography.bodyLarge)
                            }

                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(displayRating, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(" ($displayReviewCount reviews)", color = Color.Gray)
                            }
                            
                            if (w.institute.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Text("Education: ${w.institute}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("Job History & Reviews", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(12.dp))
                }

                if (reviewsLoaded && reviews.isEmpty()) {
                    item {
                        Text("No reviews yet", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                    }
                } else {
                    items(reviews) { review ->
                        ReviewItem(review)
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(review.userName, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = if (index < review.rating) Color(0xFFFFB300) else Color.LightGray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            if (review.comment.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(review.comment, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

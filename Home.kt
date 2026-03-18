package com.hanif.kajlagbe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavController) {

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val focusManager = LocalFocusManager.current

    val currentUid = auth.currentUser?.uid ?: return

    /* ---------------- STATES ---------------- */
    var workers by remember { mutableStateOf<List<Worker>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Electrician", "Plumber", "Technician", "Tailor", "Tutor", "Other")

    var filterLocation by remember { mutableStateOf("") }
    var showFilterPopup by remember { mutableStateOf(false) }
    var isWorker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        firestore.collection("workers").document(currentUid).get().addOnSuccessListener {
            isWorker = it.exists()
        }
    }

    /* ---------------- LOAD DATA ---------------- */
    DisposableEffect(Unit) {
        val listener: ListenerRegistration = firestore.collection("workers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    loading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    workers = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Worker::class.java)?.copy(uid = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    loading = false
                }
            }
        onDispose { listener.remove() }
    }

    /* ---------------- SEARCH LOGIC ---------------- */
    val filteredWorkers by remember {
        derivedStateOf {
            val query = searchText.trim().lowercase()
            val locQuery = filterLocation.trim().lowercase()
            
            workers.filter { worker ->
                val matchSearch = query.isEmpty() || 
                        worker.name.lowercase().contains(query) ||
                        worker.workType.lowercase().contains(query) ||
                        worker.location.lowercase().contains(query)

                val matchCategory = selectedCategory == "All" || 
                        worker.workType.lowercase().contains(selectedCategory.lowercase())

                val matchFilterLocation = locQuery.isEmpty() ||
                        worker.location.lowercase().contains(locQuery)

                matchSearch && matchCategory && matchFilterLocation
            }.sortedByDescending { it.isAvailable }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        Text("Kaj", fontSize = 26.sp, fontWeight = FontWeight.Black)
                        Text("Lagbe", fontSize = 26.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Emergency Logic */ }) {
                        Icon(Icons.Default.Warning, contentDescription = "Emergency", tint = Color.Red)
                    }
                    IconButton(onClick = { navController.navigate(Routes.PROFILE) }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = { navController.navigate(Routes.SETTING) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.secondary,
                onClick = {
                    if (isWorker) navController.navigate(Routes.WORKER_INBOX)
                    else navController.navigate(Routes.USER_INBOX)
                }
            ) {
                Icon(Icons.Default.Chat, contentDescription = "Inbox")
            }
        }
    ) { paddingValues ->
        
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            
            item {
                /* 1. Action Buttons */
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { navController.navigate(Routes.USER_REQUESTS) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.my_requests))
                    }

                    val nextRoute = if (isWorker) Routes.WORKER_REQUESTS else Routes.WORKER_REGISTER
                    val btnText = if (isWorker) stringResource(R.string.works) else stringResource(R.string.need_job)
                    
                    Button(
                        onClick = { navController.navigate(nextRoute) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(btnText)
                    }
                }

                /* 2. Search Bar & Filter Button */
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { searchText = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { showFilterPopup = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }

                /* 3. Categories */
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            /* 4. Results List */
            if (loading) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                if (filteredWorkers.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.no_workers), color = Color.Gray)
                        }
                    }
                } else {
                    items(filteredWorkers, key = { it.uid }) { worker ->
                        WorkerCard(worker, navController, currentUid)
                    }
                }
            }
        }
    }

    /* Filter Dialog */
    if (showFilterPopup) {
        AlertDialog(
            onDismissRequest = { showFilterPopup = false },
            title = { Text(stringResource(R.string.filter_workers)) },
            text = {
                OutlinedTextField(
                    value = filterLocation,
                    onValueChange = { filterLocation = it },
                    label = { Text(stringResource(R.string.location)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = { showFilterPopup = false }) { Text(stringResource(R.string.apply)) }
            },
            dismissButton = {
                TextButton(onClick = { filterLocation = ""; showFilterPopup = false }) { Text(stringResource(R.string.clear)) }
            }
        )
    }
}

@Composable
fun WorkerCard(worker: Worker, navController: NavController, currentUid: String) {
    val firestore = FirebaseFirestore.getInstance()
    var isBusyLive by remember { mutableStateOf(worker.isBusy) }

    LaunchedEffect(worker.uid) {
        firestore.collection("jobRequests")
            .whereEqualTo("workerId", worker.uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    isBusyLive = snapshot.documents.any { it.getString("status") == "accepted" }
                }
            }
    }

    val statusColor = when {
        !worker.isAvailable -> Color(0xFFD32F2F)
        isBusyLive -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { navController.navigate("${Routes.WORKER_DETAILS}/${worker.uid}") },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Surface(color = statusColor, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        if (!worker.isAvailable) "Unavailable" else if (isBusyLive) "Busy" else "Available",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = Color.White, fontSize = 12.sp
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "%.1f (%d reviews)".format(worker.rating, worker.reviewCount),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(Modifier.height(4.dp))
            Text("${stringResource(R.string.gender)}: ${worker.gender}", fontSize = 14.sp, color = Color.Gray)
            Text("${stringResource(R.string.institute)}: ${worker.institute}", fontSize = 14.sp, color = Color.Gray)

            Spacer(Modifier.height(4.dp))
            Text("${stringResource(R.string.work_type)}: ${worker.workType}", fontSize = 14.sp)
            Text("${stringResource(R.string.location)}: ${worker.location}", fontSize = 14.sp)

            if (worker.uid != currentUid) {
                Button(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    onClick = { navController.navigate("${Routes.REQUEST_JOB}/${worker.uid}") },
                    enabled = worker.isAvailable
                ) {
                    Text(if (!worker.isAvailable) "Unavailable" else stringResource(R.string.request_job)
                    )
                }
            }
        }
    }
}
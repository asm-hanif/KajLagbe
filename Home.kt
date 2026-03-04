package com.hanif.kajlagbe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import com.google.firebase.firestore.ListenerRegistration

/* ---------------- WORKER MODEL ---------------- */
data class Worker(
    val uid: String = "",
    val name: String = "",
    val gender: String = "",
    val workType: String = "",
    val institute: String = "",
    val contact: String = "",
    val location: String = ""
)

/* ---------------- HOME SCREEN ---------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavController) {

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val currentUid = auth.currentUser?.uid ?: return

    val darkBlue = Color(0xFF0D1B2A)
    val orange = Color(0xFFFF8C00)

    /* ---------------- STATES ---------------- */
    var workers by remember { mutableStateOf<List<Worker>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    /* SEARCH */
    var searchText by remember { mutableStateOf("") }

    /* FILTER INPUTS */
    var filterWorkType by remember { mutableStateOf("") }
    var filterLocation by remember { mutableStateOf("") }

    var showFilterPopup by remember { mutableStateOf(false) }

    /* CHECK WORKER */
    var isWorker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        firestore.collection("workers")
            .document(currentUid)
            .get()
            .addOnSuccessListener {
                isWorker = it.exists()
            }
    }

    /* ---------------- LOAD WORKERS LIVE ---------------- */
    DisposableEffect(Unit) {

        val listener: ListenerRegistration =
            firestore.collection("workers")
                .addSnapshotListener { snapshot, _ ->

                    if (snapshot != null) {
                        workers = snapshot.documents.mapNotNull { doc ->
                            val worker = doc.toObject(Worker::class.java)
                            worker?.copy(uid = doc.id)
                        }
                        loading = false
                    }
                }

        onDispose { listener.remove() }
    }

    /* ---------------- FILTERED WORKERS ---------------- */
    val filteredWorkers = workers.filter { worker ->

        val matchSearch =
            searchText.isBlank() ||
                    worker.workType.contains(searchText, ignoreCase = true)

        val matchFilterType =
            filterWorkType.isBlank() ||
                    worker.workType.contains(filterWorkType, ignoreCase = true)

        val matchFilterLocation =
            filterLocation.isBlank() ||
                    worker.location.contains(filterLocation, ignoreCase = true)

        matchSearch && matchFilterType && matchFilterLocation
    }

    /* ---------------- UI ---------------- */
    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize()) {

            /* ✅ FIXED TOP BAR (WILL NOT SCROLL) */
            TopAppBar(
                title = {
                    Row {
                        Text(
                            "Kaj",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Lagbe",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = orange
                        )
                    }
                },
                actions = {

                    IconButton(onClick = {
                        navController.navigate(Routes.PROFILE)
                    }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }

                    IconButton(onClick = {
                        navController.navigate(Routes.SETTING)
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )

            /* ✅ EVERYTHING BELOW IS SCROLLABLE */
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {

                    Spacer(Modifier.height(12.dp))

                    /* ---------------- TOP BUTTONS ---------------- */
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Button(
                            onClick = {
                                navController.navigate(Routes.USER_REQUESTS)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = orange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.my_requests), color = Color.White)
                        }

                        /* USER → NEED JOB */
                        if (!isWorker) {
                            Button(
                                onClick = {
                                    navController.navigate(Routes.WORKER_REGISTER)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = darkBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.need_job), color = Color.White)
                            }
                        }

                        /* WORKER → WORKS */
                        if (isWorker) {
                            Button(
                                onClick = {
                                    navController.navigate(Routes.WORKER_REQUESTS)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = darkBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.works), color = Color.White)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    /* ---------------- SEARCH BAR ---------------- */
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    /* ---------------- FILTER BUTTON ---------------- */
                    Button(
                        onClick = { showFilterPopup = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = darkBlue)
                    ) {
                        Text(stringResource(R.string.filter), color = Color.White)
                    }

                    Spacer(Modifier.height(12.dp))
                }

                /* ---------------- WORKERS LIST ---------------- */
                if (loading) {
                    item {
                        Box(
                            Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (filteredWorkers.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.no_workers),
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    items(filteredWorkers) { worker ->
                        WorkerCard(worker, navController, currentUid)
                    }
                }
            }
        }

        /* ---------------- FLOATING INBOX BUTTON ---------------- */
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = darkBlue,
            onClick = {
                if (isWorker)
                    navController.navigate(Routes.WORKER_INBOX)
                else
                    navController.navigate(Routes.USER_INBOX)
            }
        ) {
            Icon(Icons.Default.Chat, contentDescription = "Inbox", tint = Color.White)
        }

        /* ---------------- FILTER POPUP ---------------- */
        if (showFilterPopup) {

            AlertDialog(
                onDismissRequest = { showFilterPopup = false },

                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.filter_workers), fontWeight = FontWeight.Bold)

                        IconButton(onClick = {
                            showFilterPopup = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                },

                text = {
                    Column {

                        OutlinedTextField(
                            value = filterWorkType,
                            onValueChange = { filterWorkType = it },
                            label = { Text(stringResource(R.string.work_type)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = filterLocation,
                            onValueChange = { filterLocation = it },
                            label = { Text(stringResource(R.string.location)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },

                confirmButton = {
                    Button(
                        onClick = {
                            showFilterPopup = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = darkBlue)
                    ) {
                        Text(stringResource(R.string.apply), color = Color.White)
                    }
                },

                dismissButton = {
                    TextButton(
                        onClick = {
                            filterWorkType = ""
                            filterLocation = ""
                            showFilterPopup = false
                        }
                    ) {
                        Text(stringResource(R.string.clear))
                    }
                }
            )
        }
    }
}

/* ---------------- WORKER CARD ---------------- */
@Composable
fun WorkerCard(
    worker: Worker,
    navController: NavController,
    currentUid: String
) {

    val darkBlue = Color(0xFF0D1B2A)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                worker.name,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(Modifier.height(6.dp))

            Text("${stringResource(R.string.gender)}: ${worker.gender}")
            Text("${stringResource(R.string.work_type)}: ${worker.workType}")
            Text("${stringResource(R.string.location)}: ${worker.location}")
            Text("${stringResource(R.string.institute)}: ${worker.institute}")
            Text("${stringResource(R.string.contact)}: ${worker.contact}")

            Spacer(Modifier.height(14.dp))

            /* ✅ WORKER CANNOT REQUEST HIMSELF */
            if (worker.uid == currentUid) {

                Text(
                    stringResource(R.string.this_is_you),
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )

            } else {

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = darkBlue),
                    onClick = {
                        navController.navigate("${Routes.REQUEST_JOB}/${worker.uid}")
                    }
                ) {
                    Text(stringResource(R.string.request_job), color = Color.White)
                }
            }
        }
    }
}

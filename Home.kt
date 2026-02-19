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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/* ---------------- WORKER MODEL (unchanged) ---------------- */
data class Worker(
    val uid: String = "",
    val name: String = "",
    val gender: String = "",
    val workType: String = "",
    val institute: String = "",
    val contact: String = "",
    val location: String = ""
)

/* ---------------- DUMMY DATA & LOCAL USER ---------------- */
// Hardcoded current user (simulate logged-in user)
val CURRENT_USER_ID = "user123"
val CURRENT_USER_IS_WORKER = false // set to true to test worker-specific UI

// Dummy workers list (replaces Firestore)
val DUMMY_WORKERS = listOf(
    Worker(
        uid = "worker1",
        name = "Rahim Mia",
        gender = "Male",
        workType = "Plumber",
        institute = "Dhaka Polytechnic",
        contact = "01712345678",
        location = "Mirpur"
    ),
    Worker(
        uid = "worker2",
        name = "Karima Begum",
        gender = "Female",
        workType = "Electrician",
        institute = "Technical Training Center",
        contact = "01898765432",
        location = "Uttara"
    ),
    Worker(
        uid = "worker3",
        name = "Shofiq Ahmed",
        gender = "Male",
        workType = "Carpenter",
        institute = "Mohammadpur Technical",
        contact = "01911223344",
        location = "Mohammadpur"
    ),
    Worker(
        uid = "worker4",
        name = "Nasrin Sultana",
        gender = "Female",
        workType = "Painter",
        institute = "Shilpakala Academy",
        contact = "01655577788",
        location = "Dhanmondi"
    ),
    Worker(
        uid = "worker5",
        name = "Abdul Karim",
        gender = "Male",
        workType = "Mason",
        institute = "BUET",
        contact = "01533445566",
        location = "Motijheel"
    )
)

/* ---------------- HOME SCREEN ---------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavController) {

    // No Firebase – we use the hardcoded current user
    val currentUid = CURRENT_USER_ID
    val isWorker = CURRENT_USER_IS_WORKER

    val darkBlue = Color(0xFF0D1B2A)
    val orange = Color(0xFFFF8C00)

    /* ---------------- STATES ---------------- */
    // Load dummy workers immediately (no loading from network)
    val workers = remember { mutableStateListOf<Worker>().apply { addAll(DUMMY_WORKERS) } }
    var loading by remember { mutableStateOf(false) } // no loading needed, but kept for compatibility

    /* SEARCH */
    var searchText by remember { mutableStateOf("") }

    /* FILTER INPUTS */
    var filterWorkType by remember { mutableStateOf("") }
    var filterLocation by remember { mutableStateOf("") }

    var showFilterPopup by remember { mutableStateOf(false) }

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

            /* TOP BAR (fixed) */
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

            /* SCROLLABLE CONTENT */
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {

                    Spacer(Modifier.height(12.dp))

                    /* TOP BUTTONS */
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
                            Text("My Requests", color = Color.White)
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
                                Text("Need Job?", color = Color.White)
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
                                Text("Works", color = Color.White)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    /* SEARCH BAR */
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Search workers by work type...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    /* FILTER BUTTON */
                    Button(
                        onClick = { showFilterPopup = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = darkBlue)
                    ) {
                        Text("Filter", color = Color.White)
                    }

                    Spacer(Modifier.height(12.dp))
                }

                /* WORKERS LIST */
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
                            "No workers found 😶",
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

        /* FLOATING INBOX BUTTON */
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

        /* FILTER POPUP */
        if (showFilterPopup) {

            AlertDialog(
                onDismissRequest = { showFilterPopup = false },

                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filter Workers", fontWeight = FontWeight.Bold)

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
                            label = { Text("Work Type") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = filterLocation,
                            onValueChange = { filterLocation = it },
                            label = { Text("Location") },
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
                        Text("Apply", color = Color.White)
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
                        Text("Clear")
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

            Text("Gender: ${worker.gender}")
            Text("Work Type: ${worker.workType}")
            Text("Location: ${worker.location}")
            Text("Institute: ${worker.institute}")
            Text("Contact: ${worker.contact}")

            Spacer(Modifier.height(14.dp))

            /* WORKER CANNOT REQUEST HIMSELF */
            if (worker.uid == currentUid) {

                Text(
                    "This is you 👤",
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
                    Text("Request Job", color = Color.White)
                }
            }
        }
    }
}

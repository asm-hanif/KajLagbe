package com.hanif.kajlagbe

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestJob(navController: NavController, workerId: String) {

    val firestore = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid ?: return
    val context = LocalContext.current

    var location by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var scheduledDate by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var isLocationLoading by remember { mutableStateOf(false) }
    var workerName by remember { mutableStateOf("Worker") }
    var workType by remember { mutableStateOf("") }
    var realUserName by remember { mutableStateOf(user.displayName ?: "User") }

    /* Fetch Latest User Name & Worker Details */
    LaunchedEffect(workerId) {
        firestore.collection("users").document(userId).get().addOnSuccessListener { doc ->
            doc.getString("name")?.let { realUserName = it }
        }

        firestore.collection("workers").document(workerId).get().addOnSuccessListener { doc ->
            workerName = doc.getString("name") ?: "Worker"
            workType = doc.getString("workType") ?: ""
        }
    }

    /* ---------------- GPS & PERMISSION HANDLERS ---------------- */
    val gpsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            isLocationLoading = true
            LocationUtils.getCurrentAddress(context, { addr ->
                location = addr
                isLocationLoading = false
            }, { err ->
                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                isLocationLoading = false
            }, { /* Resolution handled */ })
        }
    }

    fun fetchAddress() {
        isLocationLoading = true
        LocationUtils.getCurrentAddress(context, { addr ->
            location = addr
            isLocationLoading = false
        }, { err ->
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            isLocationLoading = false
        }, { exception ->
            gpsLauncher.launch(IntentSenderRequest.Builder(exception.resolution).build())
            isLocationLoading = false
        })
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            fetchAddress()
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestLocation() {
        val fineLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        
        if (fineLoc == PackageManager.PERMISSION_GRANTED || coarseLoc == PackageManager.PERMISSION_GRANTED) {
            fetchAddress()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // Auto-request location if permission already exists
    LaunchedEffect(Unit) {
        val fineLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fineLoc == PackageManager.PERMISSION_GRANTED || coarseLoc == PackageManager.PERMISSION_GRANTED) {
            fetchAddress()
        }
    }

    // Date Picker Dialog
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            scheduledDate = "$dayOfMonth/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Work") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Request $workerName",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(Modifier.height(32.dp))

            /* Booking Calendar: Date Selection */
            OutlinedTextField(
                value = scheduledDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Date") },
                modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                shape = MaterialTheme.shapes.medium,
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pick Date")
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            /* Location Field with Auto-fill Icon */
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(stringResource(R.string.location)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    placeholder = { Text("e.g. House 12, Road 5, Block C...") }
                )
                
                if (isLocationLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).align(Alignment.CenterEnd).padding(end = 12.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = { requestLocation() },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Get Current Location", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.note)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = MaterialTheme.shapes.medium,
                placeholder = { Text("Describe what work needs to be done...") }
            )

            Spacer(Modifier.height(32.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !loading,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                onClick = {

                    if (location.isBlank() || note.isBlank() || scheduledDate.isBlank()) {
                        Toast.makeText(context, "Please fill all fields and select a date", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    loading = true
                    val docRef = firestore.collection("jobRequests").document()
                    val requestId = docRef.id

                    val requestData = JobRequest(
                        requestId = requestId,
                        userId = userId,
                        userName = realUserName,
                        workerId = workerId,
                        workerName = workerName,
                        workType = workType,
                        location = location,
                        note = note,
                        status = "pending",
                        timestamp = System.currentTimeMillis(),
                        scheduledDate = scheduledDate
                    )

                    docRef.set(requestData)
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
                if (loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        stringResource(R.string.request_job),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

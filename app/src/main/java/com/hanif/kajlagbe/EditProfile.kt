package com.hanif.kajlagbe

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfile(navController: NavController) {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    val user = auth.currentUser
    val uid = user?.uid
    val email = user?.email ?: ""

    /* ---------------- STATES ---------------- */
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var institute by remember { mutableStateOf("") }
    var workType by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(false) } 
    var password by remember { mutableStateOf("") }
    var isWorker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isLocationLoading by remember { mutableStateOf(false) }

    var showDeleteUserDialog by remember { mutableStateOf(false) }
    var showDeleteWorkerDialog by remember { mutableStateOf(false) }
    var showReAuthDialog by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val genderOptions = listOf("Male", "Female", "Other")

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
            }, { })
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

    /* ---------------- LOAD PROFILE ---------------- */
    LaunchedEffect(uid) {
        if (uid == null) return@LaunchedEffect
        
        // Always load basic user info first
        firestore.collection("users").document(uid).get().addOnSuccessListener { userDoc ->
            name = userDoc.getString("name") ?: user?.displayName ?: ""
            
            // Then check if worker
            firestore.collection("workers").document(uid).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    isWorker = true
                    // Name from worker doc takes precedence if it exists, but we'll sync them
                    name = doc.getString("name") ?: name 
                    gender = doc.getString("gender") ?: ""
                    institute = doc.getString("institute") ?: ""
                    workType = doc.getString("workType") ?: ""
                    contact = doc.getString("contact") ?: ""
                    location = doc.getString("location") ?: ""
                    isAvailable = doc.getBoolean("isAvailable") ?: false
                }
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_profile)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Kaj", fontSize = 34.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                Text("Lagbe", fontSize = 34.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(24.dp))

            /* ---------------- FORM ---------------- */
            InputField(stringResource(R.string.name), name) { name = it }

            InputField(
                label = stringResource(R.string.email),
                value = email,
                enabled = false
            ) {}

            if (isWorker) {
                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Available for work", fontWeight = FontWeight.Bold)
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { isAvailable = it }
                    )
                }

                Spacer(Modifier.height(8.dp))
                DropdownField(stringResource(R.string.gender), genderOptions, gender) { gender = it }
                InputField(stringResource(R.string.institute), institute) { institute = it }
                InputField(stringResource(R.string.work_type), workType) { workType = it }
                InputField(
                    label = stringResource(R.string.contact),
                    value = contact,
                    keyboardType = KeyboardType.Phone
                ) { contact = it }
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    InputField(
                        label = stringResource(R.string.location),
                        value = location
                    ) { location = it }
                    
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
            }

            Spacer(Modifier.height(32.dp))

            /* ---------------- SAVE BUTTON ---------------- */
            Button(
                onClick = {
                    if (uid == null) return@Button
                    
                    val batch = firestore.batch()
                    
                    // 1. Sync name to Auth Profile
                    user?.updateProfile(userProfileChangeRequest { displayName = name })

                    // 2. Update basic 'users' collection (Critical for hirer name in reviews)
                    val userData = mapOf("name" to name, "uid" to uid)
                    batch.set(firestore.collection("users").document(uid), userData, SetOptions.merge())

                    // 3. Update 'workers' collection if applicable
                    if (isWorker) {
                        val workerData = mapOf(
                            "uid" to uid,
                            "name" to name,
                            "gender" to gender,
                            "institute" to institute,
                            "workType" to workType,
                            "contact" to contact,
                            "location" to location,
                            "isAvailable" to isAvailable
                        )
                        batch.set(firestore.collection("workers").document(uid), workerData, SetOptions.merge())
                    }

                    batch.commit().addOnSuccessListener {
                        Toast.makeText(context, "Profile Updated ✅", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            if (isWorker) {
                OutlinedButton(
                    onClick = { showDeleteWorkerDialog = true },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Worker Profile", fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = { showDeleteUserDialog = true },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Account", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    /* ---------------- DIALOGS ---------------- */
    if (showDeleteWorkerDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteWorkerDialog = false },
            title = { Text("Delete Worker Profile?") },
            text = { Text("This will remove your worker account but keep your user account.") },
            confirmButton = {
                TextButton(onClick = {
                    firestore.collection("workers").document(uid!!).delete().addOnSuccessListener {
                        Toast.makeText(context, "Worker Profile Deleted ✅", Toast.LENGTH_LONG).show()
                        navController.navigate(Routes.HOME) { popUpTo(0) }
                    }
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteWorkerDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteUserDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteUserDialog = false },
            title = { Text("Delete Account") },
            text = { Text("Enter password to confirm deletion.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteUserDialog = false
                    showReAuthDialog = true
                }) {
                    Text("Continue", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteUserDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showReAuthDialog) {
        AlertDialog(
            onDismissRequest = { showReAuthDialog = false },
            title = { Text("Confirm Password") },
            text = {
                Column {
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (password.isBlank()) {
                        passwordError = "Password required"
                        return@TextButton
                    }
                    val credential = EmailAuthProvider.getCredential(email, password)
                    user?.reauthenticate(credential)?.addOnSuccessListener {
                        firestore.collection("users").document(uid!!).delete()
                        user.delete().addOnSuccessListener {
                            Toast.makeText(context, "Account Deleted ✅", Toast.LENGTH_LONG).show()
                            navController.navigate(Routes.WELCOME) { popUpTo(0) { inclusive = true } }
                        }
                    }?.addOnFailureListener {
                        passwordError = "Wrong password"
                    }
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReAuthDialog = false }) { Text("Cancel") }
            }
        )
    }
}

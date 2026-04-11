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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerRegister(navController: NavController) {

    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val uid = auth.currentUser?.uid ?: return
    val email = auth.currentUser?.email ?: ""

    /* ---------------- INPUT STATES ---------------- */
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var workType by remember { mutableStateOf("") }
    var institute by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var isLocationLoading by remember { mutableStateOf(false) }

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

    /* Auto-fetch location if already permitted */
    LaunchedEffect(Unit) {
        val fineLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fineLoc == PackageManager.PERMISSION_GRANTED || coarseLoc == PackageManager.PERMISSION_GRANTED) {
            fetchAddress()
        }
    }

    /* ---------------- UI ---------------- */
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* ---------------- APP TITLE ---------------- */
            Row {
                Text(
                    "Kaj",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    "Lagbe",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.worker_registration),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(Modifier.height(24.dp))

            /* ---------------- FORM ---------------- */
            InputField(stringResource(R.string.name), name) { name = it }
            
            Spacer(Modifier.height(8.dp))

            /* ---------------- GENDER DROPDOWN ---------------- */
            var genderExpanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded }
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.gender)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = MaterialTheme.shapes.medium
                )

                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    listOf("Male", "Female", "Others").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                gender = option
                                genderExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            InputField(stringResource(R.string.work_type), workType) { workType = it }
            
            Spacer(Modifier.height(8.dp))

            InputField(stringResource(R.string.institute), institute) { institute = it }
            
            Spacer(Modifier.height(8.dp))

            InputField(stringResource(R.string.contact), contact) { contact = it }
            
            Spacer(Modifier.height(8.dp))

            /* Location Field with Auto-fill Icon */
            Box(modifier = Modifier.fillMaxWidth()) {
                InputField(stringResource(R.string.location), location) { location = it }
                
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

            /* ---------------- EMAIL FIXED FIELD ---------------- */
            OutlinedTextField(
                value = email,
                onValueChange = {},
                enabled = false,
                label = { Text(stringResource(R.string.email)) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(Modifier.height(32.dp))

            /* ---------------- REGISTER BUTTON ---------------- */
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = MaterialTheme.shapes.medium,
                onClick = {

                    if (name.isBlank() || gender.isBlank() ||
                        workType.isBlank() || institute.isBlank() ||
                        contact.isBlank() || location.isBlank()
                    ) {
                        Toast.makeText(context, context.getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    loading = true

                    val workerData = Worker(
                        uid = uid,
                        name = name,
                        gender = gender,
                        workType = workType,
                        institute = institute,
                        contact = contact,
                        location = location,
                        isAvailable = true,
                        isBusy = false,
                        rating = 0f,
                        reviewCount = 0
                    )

                    firestore.collection("workers")
                        .document(uid)
                        .set(workerData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Registered Successfully ✅", Toast.LENGTH_LONG).show()
                            loading = false
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_LONG).show()
                            loading = false
                        }
                }
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onSecondary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(stringResource(R.string.register), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        singleLine = true
    )
}

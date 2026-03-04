package com.hanif.kajlagbe

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfile(navController: NavController) {

    val context = LocalContext.current

    val darkBlue = Color(0xFF0D1B2A)
    val orange = Color(0xFFFF8C00)
    val dangerRed = Color(0xFFD32F2F)

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    val user = auth.currentUser
    val uid = user?.uid
    val email = user?.email ?: ""

    /* ---------------- STATES ---------------- */

    var name by remember { mutableStateOf("") }

    // Worker-only fields
    var gender by remember { mutableStateOf("") }
    var institution by remember { mutableStateOf("") }
    var workType by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    // Password change (Removed from profile - now in settings)
    var password by remember { mutableStateOf("") }

    // Role check
    var isWorker by remember { mutableStateOf(false) }

    // UI States
    var isLoading by remember { mutableStateOf(true) }

    var showDeleteUserDialog by remember { mutableStateOf(false) }
    var showDeleteWorkerDialog by remember { mutableStateOf(false) }

    var showReAuthDialog by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val genderOptions = listOf("Male", "Female", "Other")
    val workOptions = listOf("Electrician", "Plumber", "Technician", "Tailor", "Tutor", "Other")

    /* ---------------- LOAD PROFILE ---------------- */
    LaunchedEffect(uid) {

        if (uid == null) return@LaunchedEffect

        // Check Worker Document Exists
        firestore.collection("workers")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                if (doc.exists()) {
                    isWorker = true

                    // Load Worker Data
                    name = doc.getString("name") ?: ""
                    gender = doc.getString("gender") ?: ""
                    institution = doc.getString("institution") ?: ""
                    workType = doc.getString("workType") ?: ""
                    contact = doc.getString("contact") ?: ""
                    location = doc.getString("location") ?: ""
                } else {
                    isWorker = false

                    // Normal User → only name stored in users collection
                    firestore.collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            name = userDoc.getString("name") ?: ""
                        }
                }

                isLoading = false
            }
    }

    /* ---------------- LOADING ---------------- */
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    /* ---------------- UI ---------------- */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /* Header */
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Kaj", fontSize = 34.sp, fontWeight = FontWeight.Black, color = darkBlue)
            Text("Lagbe", fontSize = 34.sp, fontWeight = FontWeight.Black, color = orange)
        }

        Spacer(Modifier.height(24.dp))

        /* ---------------- COMMON (USER + WORKER) ---------------- */

        InputField("Name", name) { name = it }

        InputField(
            label = "Email",
            value = email,
            enabled = false
        ) {}

        Spacer(Modifier.height(10.dp))

        /* ---------------- WORKER ONLY EDIT ---------------- */
        if (isWorker) {

            DropdownField("Gender", genderOptions, gender) { gender = it }

            InputField("Institution", institution) { institution = it }

            DropdownField("Work Type", workOptions, workType) { workType = it }

            InputField(
                label = "Contact Number",
                value = contact,
                keyboardType = KeyboardType.Phone
            ) { contact = it }

            InputField("Location", location) { location = it }
        }

        Spacer(Modifier.height(20.dp))

        /* ---------------- SAVE BUTTON ---------------- */
        Button(
            onClick = {

                if (uid == null) return@Button

                if (isWorker) {
                    // Save Worker Profile
                    firestore.collection("workers")
                        .document(uid)
                        .set(
                            mapOf(
                                "uid" to uid,
                                "name" to name,
                                "gender" to gender,
                                "institution" to institution,
                                "workType" to workType,
                                "contact" to contact,
                                "location" to location
                            ),
                            SetOptions.merge()
                        )

                    Toast.makeText(context, "Worker Profile Updated ✅", Toast.LENGTH_SHORT).show()

                } else {
                    // Save User Profile
                    firestore.collection("users")
                        .document(uid)
                        .set(
                            mapOf(
                                "uid" to uid,
                                "name" to name
                            ),
                            SetOptions.merge()
                        )

                    Toast.makeText(context, "User Profile Updated ✅", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = orange)
        ) {
            Text("Save Changes", fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(Modifier.height(18.dp))

        /* ---------------- DELETE BUTTONS ---------------- */

        if (isWorker) {

            // Delete Worker Profile Only
            OutlinedButton(
                onClick = { showDeleteWorkerDialog = true },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = dangerRed)
            ) {
                Text("Delete Worker Profile", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            // Delete Full Account Only After Worker Deleted
            OutlinedButton(
                onClick = {
                    Toast.makeText(
                        context,
                        "Delete Worker Profile first ❗",
                        Toast.LENGTH_LONG
                    ).show()
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
            ) {
                Text("Delete User Account (Disabled)", fontWeight = FontWeight.Bold)
            }

        } else {

            // Normal User Delete Account
            OutlinedButton(
                onClick = { showDeleteUserDialog = true },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = dangerRed)
            ) {
                Text("Delete Account", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(40.dp))
    }

    /* ---------------- WORKER DELETE CONFIRM ---------------- */
    if (showDeleteWorkerDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteWorkerDialog = false },
            title = { Text("Delete Worker Profile?") },
            text = { Text("This will remove your worker account but keep your user account.") },
            confirmButton = {
                TextButton(onClick = {

                    firestore.collection("workers")
                        .document(uid!!)
                        .delete()

                    Toast.makeText(context, "Worker Profile Deleted ✅", Toast.LENGTH_LONG).show()

                    navController.navigate(Routes.HOME) {
                        popUpTo(0)
                    }

                }) {
                    Text("Delete", color = dangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteWorkerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    /* ---------------- USER DELETE CONFIRM ---------------- */
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
                    Text("Continue", color = dangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteUserDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    /* ---------------- PASSWORD CONFIRM DELETE ---------------- */
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
                        label = { Text("Password") }
                    )

                    passwordError?.let {
                        Text(it, color = dangerRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {

                    if (password.isBlank()) {
                        passwordError = "Password required"
                        return@TextButton
                    }

                    val credential =
                        EmailAuthProvider.getCredential(email, password)

                    user?.reauthenticate(credential)
                        ?.addOnSuccessListener {

                            firestore.collection("users")
                                .document(uid!!)
                                .delete()

                            user.delete()

                            Toast.makeText(context, "Account Deleted ✅", Toast.LENGTH_LONG).show()

                            navController.navigate(Routes.WELCOME) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        ?.addOnFailureListener {
                            passwordError = "Wrong password"
                        }

                }) {
                    Text("Delete", color = dangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReAuthDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/* ---------------- INPUT FIELD ---------------- */
@Composable
fun InputField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

/* ---------------- DROPDOWN ---------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {

        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelect(it)
                        expanded = false
                    }
                )
            }
        }
    }

    Spacer(Modifier.height(6.dp))
}

package com.hanif.kajlagbe

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val darkBlue = Color(0xFF0D1B2A)
    val orange = Color(0xFFFF8C00)

    /* ---------------- INPUT STATES ---------------- */
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var workType by remember { mutableStateOf("") }
    var institute by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }

    /* ---------------- UI ---------------- */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // ✅ SCROLL FIX ADDED
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /* ---------------- APP TITLE ---------------- */
        Row {
            Text(
                "Kaj",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = darkBlue
            )
            Text(
                "Lagbe",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = orange
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Worker Registration",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        /* ---------------- FORM ---------------- */

        InputField("Full Name", name) { name = it }

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
                label = { Text("Gender") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp)
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

        Spacer(Modifier.height(10.dp))

        InputField("Work Type (Electrician, Plumber...)", workType) {
            workType = it
        }

        InputField("Institute / Company", institute) {
            institute = it
        }

        InputField("Contact Number", contact) {
            contact = it
        }

        InputField("Location", location) {
            location = it
        }

        Spacer(Modifier.height(12.dp))

        /* ---------------- EMAIL FIXED FIELD ---------------- */
        OutlinedTextField(
            value = email,
            onValueChange = {},
            enabled = false,
            label = { Text("Email (Fixed)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(25.dp))

        /* ---------------- REGISTER BUTTON ---------------- */
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),

            enabled = !loading,

            colors = ButtonDefaults.buttonColors(containerColor = darkBlue),

            shape = RoundedCornerShape(14.dp),

            onClick = {

                /* ---------------- VALIDATION ---------------- */
                if (name.isBlank() || gender.isBlank() ||
                    workType.isBlank() || institute.isBlank() ||
                    contact.isBlank() || location.isBlank()
                ) {
                    Toast.makeText(
                        context,
                        "Please fill all fields ❗",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                loading = true

                /* ---------------- SAVE WORKER DATA ---------------- */
                val workerData = mapOf(
                    "uid" to uid,
                    "name" to name,
                    "gender" to gender,
                    "workType" to workType,
                    "institute" to institute,
                    "contact" to contact,
                    "location" to location
                )

                firestore.collection("workers")
                    .document(uid)
                    .set(workerData)
                    .addOnSuccessListener {

                        Toast.makeText(
                            context,
                            "Registered Successfully ✅",
                            Toast.LENGTH_LONG
                        ).show()

                        loading = false

                        /* ✅ BACK TO HOME */
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                    .addOnFailureListener {

                        Toast.makeText(
                            context,
                            "Failed: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()

                        loading = false
                    }
            }
        ) {

            if (loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text("Register as Worker", fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(30.dp)) // ✅ Extra bottom spacing
    }
}

/* ---------------- INPUT FIELD COMPONENT ---------------- */
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp)
    )
}

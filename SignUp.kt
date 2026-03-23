package com.hanif.kajlagbe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignUp(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rePassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Strings from resources
    val allFieldsRequired = stringResource(R.string.all_fields_required)
    val passwordLengthError = stringResource(R.string.password_length_error)
    val passwordsDoNotMatch = stringResource(R.string.passwords_do_not_match)
    val emailExists = stringResource(R.string.email_exists)
    val noInternet = stringResource(R.string.no_internet)
    val signUpFailed = stringResource(R.string.sign_up_failed)
    val userSaveFailedTemplate = stringResource(R.string.user_save_failed)

    /* ✅ Auto Skip Signup if Already Logged In */
    LaunchedEffect(Unit) {
        if (auth.currentUser != null) {
            navController.navigate(Routes.HOME) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* 🔹 KajLagbe Logo */
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Kaj",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    "Lagbe",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            /* 🔹 Title */
            Text(
                text = stringResource(R.string.signup),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            /* 🔹 Input Fields */
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = rePassword,
                onValueChange = { rePassword = it },
                label = { Text(stringResource(R.string.reenter_password)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = MaterialTheme.shapes.medium
            )

            /* 🔴 Error Message */
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            /* 🔹 SignUp Button */
            Button(
                onClick = {
                    val cleanName = name.trim()
                    val cleanEmail = email.trim()
                    val cleanPassword = password.trim()
                    val cleanRePassword = rePassword.trim()

                    when {
                        cleanName.isBlank() || cleanEmail.isBlank()
                                || cleanPassword.isBlank() || cleanRePassword.isBlank() -> {
                            errorMessage = allFieldsRequired
                        }
                        cleanPassword.length < 6 -> {
                            errorMessage = passwordLengthError
                        }
                        cleanPassword != cleanRePassword -> {
                            errorMessage = passwordsDoNotMatch
                        }
                        else -> {
                            isLoading = true
                            errorMessage = ""

                            auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser
                                        val uid = user?.uid ?: return@addOnCompleteListener
                                        
                                        // Update Firebase Auth Display Name
                                        val profileUpdates = userProfileChangeRequest {
                                            displayName = cleanName
                                        }
                                        user.updateProfile(profileUpdates)

                                        val userData = mapOf(
                                            "uid" to uid,
                                            "name" to cleanName,
                                            "email" to cleanEmail,
                                            "role" to "user",
                                            "createdAt" to System.currentTimeMillis()
                                        )
                                        firestore.collection("users")
                                            .document(uid)
                                            .set(userData)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                navController.navigate(Routes.HOME) {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            }
                                            .addOnFailureListener {
                                                isLoading = false
                                                errorMessage = userSaveFailedTemplate.format(it.message)
                                            }
                                    } else {
                                        isLoading = false
                                        errorMessage = when {
                                            task.exception?.message?.contains("email") == true -> emailExists
                                            task.exception?.message?.contains("network") == true -> noInternet
                                            else -> signUpFailed
                                        }
                                    }
                                }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                enabled = !isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onSecondary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        stringResource(R.string.signup),
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            /* 🔹 Login Link */
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.already_have_account),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = stringResource(R.string.login),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        navController.navigate(Routes.LOGIN)
                    }
                )
            }
        }
    }
}

package com.puc.kajlagbe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignUp(navController: NavController) {

    val darkBlue = Color(0xFF0D1B2A)
    val orange = Color(0xFFFF8C00)

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rePassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    /* ✅ Auto Skip Signup if Already Logged In */
    LaunchedEffect(Unit) {
        if (auth.currentUser != null) {
            navController.navigate(Routes.HOME) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /* 🔹 KajLagbe Logo */
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Kaj",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                color = darkBlue
            )
            Text(
                "Lagbe",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                color = orange
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        /* 🔹 Title */
        Row {
            Text(
                "Sign",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = darkBlue
            )
            Text(
                "Up",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = orange
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        /* 🔹 Input Fields */
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = rePassword,
            onValueChange = { rePassword = it },
            label = { Text("Re-enter Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        /* 🔴 Error Message */
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = errorMessage,
                color = Color.Red,
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
                        errorMessage = "All fields are required"
                    }

                    cleanPassword.length < 6 -> {
                        errorMessage = "Password must be at least 6 characters"
                    }

                    cleanPassword != cleanRePassword -> {
                        errorMessage = "Passwords do not match"
                    }

                    else -> {

                        isLoading = true
                        errorMessage = ""

                        auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword)
                            .addOnCompleteListener { task ->

                                if (task.isSuccessful) {

                                    val uid =
                                        auth.currentUser?.uid ?: return@addOnCompleteListener

                                    /* ✅ Save User Data in Firestore */
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
                                            errorMessage =
                                                "User saved failed: ${it.message}"
                                        }

                                } else {

                                    isLoading = false

                                    errorMessage = when {
                                        task.exception?.message?.contains("email") == true ->
                                            "Email already exists ❌"

                                        task.exception?.message?.contains("network") == true ->
                                            "No internet connection 🌐"

                                        else ->
                                            "Sign up failed. Try again!"
                                    }
                                }
                            }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = darkBlue),
            enabled = !isLoading
        ) {

            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    "Sign Up",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        /* 🔹 Login Link */
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Already have an account? ",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Text(
                text = "Login",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = orange,
                modifier = Modifier.clickable {
                    navController.navigate(Routes.LOGIN)
                }
            )
        }
    }
}

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

@Composable
fun SignUp(navController: NavController) {

    val darkBlue = Color(0xFF0D1B2A)
    val orange = Color(0xFFFF8C00)

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rePassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }

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

        /* 🔹 SignUp Button (no Firebase) */
        Button(
            onClick = {
                // Basic validation only
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
                        errorMessage = "" // clear error
                        // TODO: Handle signup without Firebase
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = darkBlue)
        ) {
            Text(
                "Sign Up",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
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

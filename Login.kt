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
fun Login(navController: NavController) {

    val darkBlue = Color(0xFF0D1B2A)
    val orange = Color(0xFFFF8C00)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /* 🔹 KajLagbe Logo */
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Kaj", fontSize = 34.sp, fontWeight = FontWeight.Black, color = darkBlue)
            Text("Lagbe", fontSize = 34.sp, fontWeight = FontWeight.Black, color = orange)
        }

        Spacer(modifier = Modifier.height(48.dp))

        /* 🔹 Login Title */
        Row {
            Text("Log", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = darkBlue)
            Text("in", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = orange)
        }

        Spacer(modifier = Modifier.height(24.dp))

        /* 🔹 Email Field */
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(14.dp))

        /* 🔹 Password Field */
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        /* 🔴 Error Message (optional placeholder) */
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = errorMessage, color = Color.Red, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        /* 🔹 Login Button (no Firebase logic) */
        Button(
            onClick = {
                // Placeholder action – you can add validation or a simple toast later
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Please enter email and password"
                } else {
                    errorMessage = "" // clear any previous error
                    // TODO: Handle login without Firebase
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = orange)
        ) {
            Text("Login", fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(18.dp))

        /* 🔹 Sign Up Link (kept for navigation) */
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account? ", fontSize = 14.sp, color = Color.Gray)

            Text(
                "SignUp",
                fontWeight = FontWeight.Bold,
                color = orange,
                modifier = Modifier.clickable {
                    navController.navigate(Routes.SIGNUP)
                }
            )
        }
    }
}

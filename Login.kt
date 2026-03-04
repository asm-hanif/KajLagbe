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

@Composable
fun Login(navController: NavController) {

    val darkBlue = Color(0xFF0D1B2A)
    val orange = Color(0xFFFF8C00)

    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Pre-calculate strings for use inside non-composable lambdas
    val isEnglish = stringResource(R.string.logout) == "Log Out"
    val allFieldsRequired = stringResource(R.string.all_fields_required)
    val wrongPassword = stringResource(R.string.wrong_password)
    val accountNotFound = stringResource(R.string.account_not_found)
    val noInternet = stringResource(R.string.no_internet)
    val loginFailed = stringResource(R.string.login_failed)

    /* ✅ Auto Skip Login if Already Logged In */
    LaunchedEffect(Unit) {
        if (auth.currentUser != null) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.LOGIN) { inclusive = true }
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
                Text("Kaj", fontSize = 34.sp, fontWeight = FontWeight.Black, color = darkBlue)
                Text("Lagbe", fontSize = 34.sp, fontWeight = FontWeight.Black, color = orange)
            }

            Spacer(modifier = Modifier.height(48.dp))

            /* 🔹 Login Title */
            Row {
                Text(
                    text = if (isEnglish) "Log" else "লগ",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkBlue
                )
                Text(
                    text = if (isEnglish) "in" else "ইন",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = orange
                )
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
                label = { Text(if (isEnglish) "Password" else "পাসওয়ার্ড") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            /* 🔴 Error Message */
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = errorMessage, color = Color.Red, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            /* 🔹 Login Button */
            Button(
                onClick = {

                    val cleanEmail = email.trim()
                    val cleanPassword = password.trim()

                    if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
                        errorMessage = allFieldsRequired
                        return@Button
                    }

                    isLoading = true
                    errorMessage = ""

                    auth.signInWithEmailAndPassword(cleanEmail, cleanPassword)
                        .addOnCompleteListener { task ->

                            isLoading = false

                            if (task.isSuccessful) {

                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }

                            } else {

                                errorMessage = when {
                                    task.exception?.message?.contains("password") == true -> wrongPassword
                                    task.exception?.message?.contains("no user") == true -> accountNotFound
                                    task.exception?.message?.contains("network") == true -> noInternet
                                    else -> loginFailed
                                }
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = orange),
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
                        text = if (isEnglish) "Login" else "লগইন",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            /* 🔹 Sign Up Link */
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isEnglish) "Don't have an account? " else "অ্যাকাউন্ট নেই? ",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Text(
                    text = if (isEnglish) "SignUp" else "সাইন আপ",
                    fontWeight = FontWeight.Bold,
                    color = orange,
                    modifier = Modifier.clickable {
                        navController.navigate(Routes.SIGNUP)
                    }
                )
            }
        }
    }
}

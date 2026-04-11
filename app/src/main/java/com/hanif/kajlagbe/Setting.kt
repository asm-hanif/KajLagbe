package com.hanif.kajlagbe

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun Setting(navController: NavController) {

    val context = LocalContext.current
    val activity = context as Activity
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    /* 🔥 LOAD SAVED LANGUAGE USING DATASTORE */
    val language by LanguageStore.getLanguage(context).collectAsStateWithLifecycle(initialValue = "en")
    val isEnglish = language == "en"

    /* 🔐 Change Password Dialog State */
    var showChangePassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /* 🔹 Header */
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Kaj", fontSize = 34.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
            Text("Lagbe", fontSize = 34.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(48.dp))

        /* 🔹 Language Title */
        Text(
            text = stringResource(R.string.language),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        /* 🔹 Language Switch */
        Button(
            onClick = {
                val newLang = if (isEnglish) "bn" else "en"
                scope.launch {
                    LanguageStore.saveLanguage(context, newLang)
                    setLocale(context, newLang)
                    activity.recreate()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = stringResource(R.string.switch_language),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        /* 🔐 Change Password Button */
        Button(
            onClick = { showChangePassword = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = stringResource(R.string.change_password),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        /* 🔹 Logout */
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(Routes.WELCOME) {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(
                text = stringResource(R.string.logout),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
    }

    /* 🔐 CHANGE PASSWORD POPUP */
    if (showChangePassword) {

        var oldPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePassword = false },
            confirmButton = {},
            title = {
                Text(
                    text = stringResource(R.string.change_password),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {

                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text(stringResource(R.string.old_password)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text(stringResource(R.string.new_password)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(stringResource(R.string.reenter_password)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        TextButton(onClick = {
                            showChangePassword = false
                        }) {
                            Text(stringResource(R.string.cancel))
                        }

                        Button(onClick = {

                            /* ✅ VALIDATION (FIXES CRASH) */
                            if (oldPassword.isBlank() ||
                                newPassword.isBlank() ||
                                confirmPassword.isBlank()
                            ) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.all_fields_required),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            if (newPassword.length < 6) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.password_length_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            if (newPassword != confirmPassword) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.passwords_do_not_match),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            val user = auth.currentUser
                            val email = user?.email

                            if (user == null || email == null) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.user_not_logged_in),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            val credential =
                                EmailAuthProvider.getCredential(email, oldPassword)

                            user.reauthenticate(credential)
                                .addOnSuccessListener {
                                    user.updatePassword(newPassword)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.password_changed),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            showChangePassword = false
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                context,
                                                it.message,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.old_password_incorrect),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                        }) {
                            Text(stringResource(R.string.save_password))
                        }
                    }
                }
            }
        )
    }
}

/* ---------------- LANGUAGE HELPERS ---------------- */

fun setLocale(context: Context, language: String) {
    val locale = Locale(language)
    Locale.setDefault(locale)

    val config = context.resources.configuration
    config.setLocale(locale)

    context.resources.updateConfiguration(
        config,
        context.resources.displayMetrics
    )
}

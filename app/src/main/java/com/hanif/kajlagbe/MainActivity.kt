package com.hanif.kajlagbe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hanif.kajlagbe.ui.theme.KajLagbeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val language by LanguageStore.getLanguage(context).collectAsStateWithLifecycle(initialValue = "en")

            // ✅ Update locale when language changes
            LaunchedEffect(language) {
                setLocale(context, language)
            }

            KajLagbeTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Routes.AUTH_GATE
                ) {
                    /* ---------------- AUTH ---------------- */
                    composable(Routes.AUTH_GATE) {
                        AuthGate(navController)
                    }

                    composable(Routes.WELCOME) {
                        Welcome(navController)
                    }

                    composable(Routes.LOGIN) {
                        Login(navController)
                    }

                    composable(Routes.SIGNUP) {
                        SignUp(navController)
                    }

                    /* ---------------- MAIN ---------------- */
                    composable(Routes.HOME) {
                        Home(navController)
                    }

                    composable(Routes.PROFILE) {
                        EditProfile(navController)
                    }

                    composable(Routes.SETTING) {
                        Setting(navController)
                    }

                    /* ---------------- WORKER DETAILS ---------------- */
                    composable(
                        route = "${Routes.WORKER_DETAILS}/{workerId}"
                    ) { backStackEntry ->
                        val workerId = backStackEntry.arguments?.getString("workerId") ?: ""
                        WorkerDetails(workerId = workerId, navController = navController)
                    }

                    /* ---------------- WORKER REGISTER ---------------- */
                    composable(Routes.WORKER_REGISTER) {
                        WorkerRegister(navController)
                    }

                    /* ---------------- JOB REQUEST ---------------- */
                    composable(
                        route = "${Routes.REQUEST_JOB}/{workerId}"
                    ) { backStackEntry ->
                        val workerId = backStackEntry.arguments?.getString("workerId") ?: ""
                        RequestJob(
                            navController = navController,
                            workerId = workerId
                        )
                    }

                    /* ---------------- REQUEST LISTS ---------------- */
                    composable(Routes.USER_REQUESTS) {
                        UserRequests(navController)
                    }

                    composable(Routes.WORKER_REQUESTS) {
                        WorkerRequests(navController)
                    }

                    /* ---------------- INBOX ---------------- */
                    composable(Routes.USER_INBOX) {
                        UserInbox(navController)
                    }

                    composable(Routes.WORKER_INBOX) {
                        WorkerInbox(navController)
                    }

                    /* ---------------- CHAT ---------------- */
                    composable(
                        route = "${Routes.CHAT}/{chatId}"
                    ) { backStackEntry ->
                        val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                        ChatScreen(chatId = chatId)
                    }
                }
            }
        }
    }
}

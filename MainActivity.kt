package com.puc.kajlagbe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.puc.kajlagbe.ui.theme.KajLagbeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        val lang = getSavedLanguage(this)
        setLocale(this, lang)

        super.onCreate(savedInstanceState)

        setContent {

            KajLagbeTheme {

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Routes.LOGIN 
                ) {

                    // Only these two screens remain
                    composable(Routes.LOGIN) {
                        Login(navController)
                    }

                    composable(Routes.SIGNUP) {
                        SignUp(navController)
                    }
                }
            }
        }
    }
}

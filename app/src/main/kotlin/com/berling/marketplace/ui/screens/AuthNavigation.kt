package com.berling.marketplace.ui.screens

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.berling.marketplace.ui.screens.auth.LoginScreen
import com.berling.marketplace.ui.screens.auth.SignUpScreen
import com.berling.marketplace.ui.screens.auth.OtpVerificationScreen
import com.berling.marketplace.ui.screens.auth.PasswordResetScreen
import com.berling.marketplace.ui.screens.auth.ProfileSetupScreen

fun NavGraphBuilder.authNavigation(navController: NavHostController) {
    navigation(startDestination = "auth/login", route = "auth") {
        composable("auth/login") {
            LoginScreen(navController = navController)
        }
        composable("auth/signup") {
            SignUpScreen(navController = navController)
        }
        composable("auth/otp_verify/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            OtpVerificationScreen(navController = navController, email = email)
        }
        composable("auth/password_reset") {
            PasswordResetScreen(navController = navController)
        }
        composable("auth/profile_setup") {
            ProfileSetupScreen(navController = navController)
        }
    }
}

const val AuthNavigation = "auth"

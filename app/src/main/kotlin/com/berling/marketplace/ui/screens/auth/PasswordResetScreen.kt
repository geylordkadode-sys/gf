package com.berling.marketplace.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.berling.marketplace.ui.screens.UiEvent
import com.berling.marketplace.ui.screens.UiState
import kotlinx.coroutines.delay

@Composable
fun PasswordResetScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var otpValues by remember { mutableStateOf(List(6) { "" }) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var step by remember { mutableStateOf(0) } // 0: email, 1: otp, 2: new password
    var errorMessage by remember { mutableStateOf("") }
    var timeRemaining by remember { mutableStateOf(60) }
    var canResend by remember { mutableStateOf(false) }
    var resendCount by remember { mutableStateOf(0) }
    
    val context = LocalContext.current
    val loginState by viewModel.loginState.collectAsState()
    val events by viewModel.events.collectAsState()

    LaunchedEffect(events) {
        when (events) {
            is UiEvent.Navigate -> {
                val route = (events as UiEvent.Navigate).route
                navController.navigate(route)
                viewModel.clearEvent()
            }
            is UiEvent.ShowMessage -> {
                viewModel.clearEvent()
            }
            null -> {}
            else -> viewModel.clearEvent()
        }
    }

    LaunchedEffect(step) {
        if (step == 1) {
            // Reset timer when moving to OTP step
            timeRemaining = 60
            canResend = false
            while (timeRemaining > 0 && !canResend && step == 1) {
                delay(1000)
                timeRemaining--
            }
            if (timeRemaining == 0 && step == 1) {
                canResend = true
            }
        }
    }

    LaunchedEffect(loginState) {
        if (loginState is UiState.Error) {
            errorMessage = (loginState as UiState.Error).message
        }
    }

    val otp = otpValues.joinToString("")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Heart Icon
        Text(
            text = "💖",
            fontSize = 60.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Heading
        Text(
            text = "Forgot Password?",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Subheading
        Text(
            text = "Enter your email or phone number and we'll send you a code to reset your password",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (step) {
            0 -> {
                // Step 1: Email/Phone Entry
                OutlinedTextField(
                    value = emailOrPhone,
                    onValueChange = { emailOrPhone = it },
                    label = { Text("Email or Phone Number") },
                    placeholder = { Text("Enter email or phone") },
                    leadingIcon = { Text("📧", fontSize = 18.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )

                // Error Message
                if (errorMessage.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        when {
                            emailOrPhone.isEmpty() -> errorMessage = "Please enter email or phone number"
                            else -> {
                                errorMessage = ""
                                viewModel.resetPassword(emailOrPhone)
                                step = 1
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = loginState !is UiState.Loading
                ) {
                    if (loginState is UiState.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Send OTP", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            1 -> {
                // Step 2: OTP Verification
                Text(
                    text = "Enter the 6-digit code sent to\n${emailOrPhone.take(3)}***",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // OTP Input Fields
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(6) { index ->
                        OutlinedTextField(
                            value = otpValues[index],
                            onValueChange = { newValue ->
                                if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                                    otpValues = otpValues.toMutableList().apply { set(index, newValue) }
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.headlineSmall.copy(
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                // Error Message
                if (errorMessage.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (otp.length == 6) {
                            errorMessage = ""
                            step = 2
                        } else {
                            errorMessage = "Please enter a valid 6-digit code"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = otp.length == 6
                ) {
                    Text("Verify OTP", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Resend OTP
                if (canResend && resendCount < 3) {
                    TextButton(
                        onClick = {
                            otpValues = List(6) { "" }
                            errorMessage = ""
                            resendCount++
                            canResend = false
                            timeRemaining = 60
                        }
                    ) {
                        Text(
                            "Resend OTP",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                } else if (!canResend) {
                    Text(
                        text = "Resend OTP in ${timeRemaining}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Maximum resend attempts reached",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            2 -> {
                // Step 3: New Password Entry
                Text(
                    text = "Enter your new password",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    placeholder = { Text("Min 8 characters") },
                    leadingIcon = { Text("🔒", fontSize = 18.sp) },
                    trailingIcon = {
                        Text(
                            if (showPassword) "👁️" else "👁️‍🗨️",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    placeholder = { Text("Re-enter password") },
                    leadingIcon = { Text("🔒", fontSize = 18.sp) },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )

                // Error Message
                if (errorMessage.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        when {
                            newPassword.isEmpty() -> errorMessage = "Please enter new password"
                            newPassword.length < 8 -> errorMessage = "Password must be at least 8 characters"
                            newPassword != confirmPassword -> errorMessage = "Passwords do not match"
                            else -> {
                                errorMessage = ""
                                viewModel.confirmPasswordReset(emailOrPhone, otp, newPassword)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = loginState !is UiState.Loading
                ) {
                    if (loginState is UiState.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Reset Password", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back to Login Link
        TextButton(
            onClick = { navController.navigate("auth/login") }
        ) {
            Text(
                "Back to Login",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

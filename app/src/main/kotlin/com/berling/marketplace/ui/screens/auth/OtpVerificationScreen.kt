package com.berling.marketplace.ui.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.berling.marketplace.ui.screens.UiEvent
import com.berling.marketplace.ui.screens.UiState
import kotlinx.coroutines.delay

@Composable
fun OtpVerificationScreen(
    navController: NavHostController,
    email: String,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var otpValues by remember { mutableStateOf(List(6) { "" }) }
    var resendCount by remember { mutableStateOf(0) }
    var timeRemaining by remember { mutableStateOf(60) }
    var canResend by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
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

    LaunchedEffect(Unit) {
        while (timeRemaining > 0 && !canResend) {
            delay(1000)
            timeRemaining--
        }
        if (timeRemaining == 0) {
            canResend = true
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
            text = "Verify OTP",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Subheading
        Text(
            text = "Enter the 6-digit code sent to\n${email.take(3)}***${email.takeLastWhile { it != '@' }}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // OTP Input Fields
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(6) { index ->
                OutlinedTextField(
                    value = otpValues[index],
                    onValueChange = { newValue ->
                        if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                            otpValues = otpValues.toMutableList().apply { set(index, newValue) }
                            // Auto-focus to next field
                            if (newValue.isNotEmpty() && index < 5) {
                                // In a real implementation, you'd want to handle focus management
                            }
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

        // Verify Button
        Button(
            onClick = {
                if (otp.length == 6) {
                    viewModel.verifyOtp(email, otp, context)
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
            enabled = otp.length == 6 && loginState !is UiState.Loading
        ) {
            if (loginState is UiState.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    "Verify & Login",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Resend OTP
        if (canResend && resendCount < 3) {
            TextButton(
                onClick = {
                    // Reset OTP input
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

        Spacer(modifier = Modifier.height(16.dp))

        // Go Back Link
        TextButton(
            onClick = { navController.navigateUp() }
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

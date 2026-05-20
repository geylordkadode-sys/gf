@file:OptIn(ExperimentalMaterial3Api::class)
package com.berling.marketplace.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.berling.marketplace.ui.screens.UiState
import kotlinx.coroutines.launch

@Composable
fun PaymentAuthenticationScreen(
    viewModel: PaymentSetupViewModel = hiltViewModel(),
    onSuccess: () -> Unit = {}
) {
    val setupState by viewModel.setupState.collectAsState()
    val supportedGateways by viewModel.supportedGateways.collectAsState()
    val authorizedGateways by viewModel.authorizedGateways.collectAsState()

    var selectedGateway by remember { mutableStateOf("razorpay") }
    var expandedGateway by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Gateway Setup") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Gateway Selection
            Text(
                "Select Payment Gateway",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expandedGateway,
                onExpandedChange = { expandedGateway = !expandedGateway },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                TextField(
                    value = selectedGateway.uppercase(),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text("Gateway") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGateway) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expandedGateway,
                    onDismissRequest = { expandedGateway = false }
                ) {
                    supportedGateways.forEach { gateway ->
                        DropdownMenuItem(
                            text = { Text(gateway.uppercase()) },
                            onClick = {
                                selectedGateway = gateway
                                expandedGateway = false
                            }
                        )
                    }
                }
            }

            // Gateway-specific setup forms
            when (selectedGateway.lowercase()) {
                "razorpay" -> RazorpaySetupForm(viewModel)
                "stripe" -> StripeSetupForm(viewModel)
                "paypal" -> PayPalSetupForm(viewModel)
            }

            // Status messages
            when (setupState) {
                is UiState.Idle, is UiState.Loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                is UiState.Success -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Text(
                            (setupState as UiState.Success<String>).data,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    scope.launch {
                        kotlinx.coroutines.delay(2000)
                        onSuccess()
                    }
                }
                is UiState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            "Error: ${(setupState as UiState.Error).message}",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Authorized gateways section
            if (authorizedGateways.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    "Authorized Gateways",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                authorizedGateways.forEach { gateway ->
                    AssistChip(
                        onClick = { selectedGateway = gateway },
                        label = { Text("✓ ${gateway.uppercase()}") },
                        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RazorpaySetupForm(viewModel: PaymentSetupViewModel) {
    var keyId by remember { mutableStateOf("") }
    var keySecret by remember { mutableStateOf("") }
    var webhookUrl by remember { mutableStateOf("") }
    var showSecret by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = keyId,
            onValueChange = { keyId = it },
            label = { Text("Razorpay Key ID") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        OutlinedTextField(
            value = keySecret,
            onValueChange = { keySecret = it },
            label = { Text("Razorpay Key Secret") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            visualTransformation = if (showSecret) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showSecret = !showSecret }) {
                    Icon(
                        if (showSecret) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        "Toggle visibility"
                    )
                }
            }
        )

        OutlinedTextField(
            value = webhookUrl,
            onValueChange = { webhookUrl = it },
            label = { Text("Webhook URL (Optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )

        Button(
            onClick = {
                viewModel.setupRazorpay(keyId, keySecret, webhookUrl)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = keyId.isNotEmpty() && keySecret.isNotEmpty()
        ) {
            Text("Setup Razorpay")
        }
    }
}

@Composable
private fun StripeSetupForm(viewModel: PaymentSetupViewModel) {
    var publishableKey by remember { mutableStateOf("") }
    var secretKey by remember { mutableStateOf("") }
    var webhookSecret by remember { mutableStateOf("") }
    var showKeys by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = publishableKey,
            onValueChange = { publishableKey = it },
            label = { Text("Publishable Key") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        OutlinedTextField(
            value = secretKey,
            onValueChange = { secretKey = it },
            label = { Text("Secret Key") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            visualTransformation = if (showKeys) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showKeys = !showKeys }) {
                    Icon(
                        if (showKeys) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        "Toggle visibility"
                    )
                }
            }
        )

        OutlinedTextField(
            value = webhookSecret,
            onValueChange = { webhookSecret = it },
            label = { Text("Webhook Secret (Optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            visualTransformation = if (showKeys) VisualTransformation.None else PasswordVisualTransformation()
        )

        Button(
            onClick = {
                viewModel.setupStripe(publishableKey, secretKey, webhookSecret)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = publishableKey.isNotEmpty() && secretKey.isNotEmpty()
        ) {
            Text("Setup Stripe")
        }
    }
}

@Composable
private fun PayPalSetupForm(viewModel: PaymentSetupViewModel) {
    var clientId by remember { mutableStateOf("") }
    var clientSecret by remember { mutableStateOf("") }
    var webhookId by remember { mutableStateOf("") }
    var showSecret by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = clientId,
            onValueChange = { clientId = it },
            label = { Text("Client ID") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        OutlinedTextField(
            value = clientSecret,
            onValueChange = { clientSecret = it },
            label = { Text("Client Secret") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            visualTransformation = if (showSecret) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showSecret = !showSecret }) {
                    Icon(
                        if (showSecret) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        "Toggle visibility"
                    )
                }
            }
        )

        OutlinedTextField(
            value = webhookId,
            onValueChange = { webhookId = it },
            label = { Text("Webhook ID (Optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Button(
            onClick = {
                viewModel.setupPayPal(clientId, clientSecret, webhookId)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = clientId.isNotEmpty() && clientSecret.isNotEmpty()
        ) {
            Text("Setup PayPal")
        }
    }
}

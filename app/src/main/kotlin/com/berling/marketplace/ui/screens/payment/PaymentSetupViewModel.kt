package com.berling.marketplace.ui.screens.payment

import androidx.lifecycle.viewModelScope
import com.berling.marketplace.data.repository.PaymentRepository
import com.berling.marketplace.data.repository.AuthenticationRepository
import com.berling.marketplace.ui.screens.BaseViewModel
import com.berling.marketplace.ui.screens.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentSetupViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val authRepository: AuthenticationRepository
) : BaseViewModel() {

    private val _supportedGateways = MutableStateFlow<List<String>>(emptyList())
    val supportedGateways: StateFlow<List<String>> = _supportedGateways

    private val _authorizedGateways = MutableStateFlow<List<String>>(emptyList())
    val authorizedGateways: StateFlow<List<String>> = _authorizedGateways

    private val _setupState = MutableStateFlow<UiState<String>>(UiState.Loading)
    val setupState: StateFlow<UiState<String>> = _setupState

    init {
        _supportedGateways.value = paymentRepository.getSupportedGateways()
        loadAuthorizedGateways()
    }

    fun loadAuthorizedGateways() {
        val gateways = mutableListOf<String>()
        for (gateway in paymentRepository.getSupportedGateways()) {
            if (paymentRepository.isGatewayAuthorized(gateway)) {
                gateways.add(gateway)
            }
        }
        _authorizedGateways.value = gateways
    }

    fun setupRazorpay(keyId: String, keySecret: String, webhookUrl: String = "") {
        viewModelScope.launch {
            _setupState.emit(UiState.Loading)
            try {
                val metadata = "keyId=$keyId&webhookUrl=$webhookUrl"
                paymentRepository.savePaymentAuthorization("razorpay", keySecret, metadata)
                _setupState.emit(UiState.Success("Razorpay configured successfully"))
                loadAuthorizedGateways()
                logInfo("Razorpay setup completed")
            } catch (e: Exception) {
                _setupState.emit(UiState.Error(e.message ?: "Setup failed"))
            }
        }
    }

    fun setupStripe(publishableKey: String, secretKey: String, webhookSecret: String = "") {
        viewModelScope.launch {
            _setupState.emit(UiState.Loading)
            try {
                val metadata = "publishableKey=$publishableKey&webhookSecret=$webhookSecret"
                paymentRepository.savePaymentAuthorization("stripe", secretKey, metadata)
                _setupState.emit(UiState.Success("Stripe configured successfully"))
                loadAuthorizedGateways()
                logInfo("Stripe setup completed")
            } catch (e: Exception) {
                _setupState.emit(UiState.Error(e.message ?: "Setup failed"))
            }
        }
    }

    fun setupPayPal(clientId: String, clientSecret: String, webhookId: String = "") {
        viewModelScope.launch {
            _setupState.emit(UiState.Loading)
            try {
                val metadata = "clientId=$clientId&webhookId=$webhookId"
                paymentRepository.savePaymentAuthorization("paypal", clientSecret, metadata)
                _setupState.emit(UiState.Success("PayPal configured successfully"))
                loadAuthorizedGateways()
                logInfo("PayPal setup completed")
            } catch (e: Exception) {
                _setupState.emit(UiState.Error(e.message ?: "Setup failed"))
            }
        }
    }

    fun isGatewaySetup(gateway: String): Boolean {
        return paymentRepository.isGatewayAuthorized(gateway)
    }

    fun getGatewayMetadata(gateway: String): String? {
        return paymentRepository.getPaymentMetadata(gateway)
    }
}

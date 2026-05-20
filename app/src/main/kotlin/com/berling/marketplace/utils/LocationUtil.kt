package com.berling.marketplace.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val country: String = "",
    val postalCode: String = ""
)

object LocationUtil {
    
    private var fusedLocationClient: FusedLocationProviderClient? = null
    
    /**
     * Get current device location
     * Requires ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission
     */
    suspend fun getCurrentLocation(context: Context): LocationData? = suspendCancellableCoroutine { continuation ->
        // Check permissions
        if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }

        val cancellationToken = CancellationTokenSource()
        
        try {
            fusedLocationClient?.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            )?.addOnSuccessListener { location ->
                if (location != null) {
                    // Reverse geocode to get address
                    val geocoder = Geocoder(context)
                    try {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        val address = addresses?.firstOrNull()
                        
                        val locationData = LocationData(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            address = address?.getAddressLine(0) ?: "",
                            city = address?.locality ?: "",
                            state = address?.adminArea ?: "",
                            country = address?.countryName ?: "",
                            postalCode = address?.postalCode ?: ""
                        )
                        continuation.resume(locationData)
                    } catch (e: Exception) {
                        continuation.resume(
                            LocationData(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        )
                    }
                } else {
                    continuation.resume(null)
                }
            }?.addOnFailureListener {
                continuation.resume(null)
            }
        } catch (e: Exception) {
            continuation.resume(null)
        }
    }

    /**
     * Reverse geocode coordinates to get address
     */
    suspend fun getAddressFromLocation(
        context: Context,
        latitude: Double,
        longitude: Double
    ): LocationData? = suspendCancellableCoroutine { continuation ->
        try {
            val geocoder = Geocoder(context)
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val address = addresses?.firstOrNull()
            
            if (address != null) {
                continuation.resume(
                    LocationData(
                        latitude = latitude,
                        longitude = longitude,
                        address = address.getAddressLine(0) ?: "",
                        city = address.locality ?: "",
                        state = address.adminArea ?: "",
                        country = address.countryName ?: "",
                        postalCode = address.postalCode ?: ""
                    )
                )
            } else {
                continuation.resume(
                    LocationData(
                        latitude = latitude,
                        longitude = longitude
                    )
                )
            }
        } catch (e: Exception) {
            continuation.resume(
                LocationData(
                    latitude = latitude,
                    longitude = longitude
                )
            )
        }
    }

    /**
     * Geocode address to coordinates
     */
    suspend fun getLocationFromAddress(
        context: Context,
        address: String
    ): LocationData? = suspendCancellableCoroutine { continuation ->
        try {
            val geocoder = Geocoder(context)
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(address, 1)
            val location = addresses?.firstOrNull()
            
            if (location != null) {
                continuation.resume(
                    LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        address = location.getAddressLine(0) ?: address,
                        city = location.locality ?: "",
                        state = location.adminArea ?: "",
                        country = location.countryName ?: "",
                        postalCode = location.postalCode ?: ""
                    )
                )
            } else {
                continuation.resume(null)
            }
        } catch (e: Exception) {
            continuation.resume(null)
        }
    }

    /**
     * Check if location services are enabled
     */
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
        return locationManager?.isLocationEnabled ?: false
    }

    /**
     * Check if app has location permissions
     */
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Calculate distance between two coordinates in kilometers
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val result = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, result)
        return result[0] / 1000.0 // Convert to km
    }

    /**
     * Format location for display
     */
    fun formatLocation(locationData: LocationData): String {
        return when {
            locationData.city.isNotEmpty() && locationData.state.isNotEmpty() ->
                "${locationData.city}, ${locationData.state}"
            locationData.city.isNotEmpty() ->
                locationData.city
            locationData.address.isNotEmpty() ->
                locationData.address.split(",").firstOrNull() ?: locationData.address
            else ->
                "%.4f, %.4f".format(locationData.latitude, locationData.longitude)
        }
    }

    /**
     * Get readable address string
     */
    fun getReadableAddress(locationData: LocationData): String {
        return buildString {
            if (locationData.address.isNotEmpty()) append(locationData.address)
            if (locationData.city.isNotEmpty()) {
                if (isNotEmpty()) append(", ")
                append(locationData.city)
            }
            if (locationData.state.isNotEmpty()) {
                if (isNotEmpty()) append(", ")
                append(locationData.state)
            }
            if (locationData.country.isNotEmpty()) {
                if (isNotEmpty()) append(", ")
                append(locationData.country)
            }
        }
    }
}

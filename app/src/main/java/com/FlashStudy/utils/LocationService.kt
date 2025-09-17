package com.FlashStudy.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.FlashStudy.data.model.Location as LocationModel

class LocationService(private val context: Context) {

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    suspend fun getCurrentLocation(): Pair<Double, Double>? =
        suspendCancellableCoroutine { continuation ->
            if (!hasLocationPermission()) {
                continuation.resumeWithException(SecurityException("Permissão de localização não concedida"))
                return@suspendCancellableCoroutine
            }

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                600000L // Atualiza acada 10 min
            ).build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        continuation.resume(Pair(location.latitude, location.longitude))
                    } ?: continuation.resumeWithException(Exception("Localização não disponível"))

                    fusedLocationProviderClient.removeLocationUpdates(this)
                }
            }

            try {
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                continuation.resumeWithException(e)
            }

            continuation.invokeOnCancellation {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            }
        }

    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    object LocationUtils {
        fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
            val startPoint = Location("pointA")
            startPoint.latitude = lat1
            startPoint.longitude = lon1

            val endPoint = Location("pointB")
            endPoint.latitude = lat2
            endPoint.longitude = lon2

            return startPoint.distanceTo(endPoint)
        }

        fun isNearLocation(currentLat: Double, currentLon: Double, savedLocation: LocationModel): Boolean {
            val distance = calculateDistance(currentLat, currentLon, savedLocation.latitude, savedLocation.longitude)
            return distance <= 10f
        }
    }
}
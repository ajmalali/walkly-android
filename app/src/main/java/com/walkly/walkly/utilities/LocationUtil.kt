package com.walkly.walkly.utilities

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationUtil(activity: Activity) {
    private val activity = activity
    private var MY_PERMISSIONS_REQUEST_ACESS_FINE_LOCATION = 0
    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)

    init {
        checkPermission()
    }

    private fun checkPermission(){
        // check if the location permission is granted
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e("Location_ERROR", "permission is not granted")
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_ACESS_FINE_LOCATION)
            }
        }

    }
    fun readLocation(){
        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
            Log.d("Location_latitude", location?.latitude.toString() )
            Log.d("Location_altitude", location?.altitude.toString())
        }.addOnFailureListener { exception: Exception ->
            Log.e("Location_ERROR", exception.toString())
        }
    }
}
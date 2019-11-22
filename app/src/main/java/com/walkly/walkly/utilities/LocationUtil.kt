package com.walkly.walkly.utilities

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class LocationUtil(activity: Activity, interval: Long, fastestInterval: Long) {

    // constructor fields
    private val activity = activity
    private val interval = interval
    private val fastestInterval = fastestInterval

    // constants
    private var MY_PERMISSIONS_REQUEST_ACESS_FINE_LOCATION = System.identityHashCode(activity).and(0xFFF)
    private val TAG = "LocationUtil"

    // initialization variables
    private var fusedLocationClient: FusedLocationProviderClient
            = LocationServices.getFusedLocationProviderClient(activity)
    private val locationSettingClient: SettingsClient
            = LocationServices.getSettingsClient(activity)
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingsRequest: LocationSettingsRequest

    init {
        checkPermission()
        createLocationCallback()
        createLocationRequest()
        createLocationSettingsRequest()
    }

    // state
    private var update = false
    // observable location
    private lateinit var data: MutableLiveData<Location>

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

    fun readLocation(data: MutableLiveData<Location>) {
        this.data = data
        startLocationUpdates()
    }


    fun startLocationUpdates() {
        if (!update) {
            update = true
            checkSettings()
        }
    }
    fun stopLocationUpdates(){
        if (update) {
        fusedLocationClient.removeLocationUpdates(locationCallback)
            .addOnCompleteListener {
                update = false
                Log.d(TAG, "stopped updates")
            }
        }
    }


    fun createLocationCallback(){
        locationCallback = object: LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

            //    location = locationResult?.lastLocation
                data.value = locationResult?.lastLocation

            }
        }
    }
    fun createLocationRequest(){
        locationRequest = LocationRequest()
        // sets how frequent the app. will get location updates
        // the value is not exact. it may receives faster updates
        locationRequest.interval = interval
        locationRequest.fastestInterval = fastestInterval
        // requires gps location
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    fun createLocationSettingsRequest(){
        locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()
    }

    fun checkSettings(){
        locationSettingClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
                Log.d(TAG, "started updates")
            }
            .addOnFailureListener {
                when((it as ApiException).statusCode){
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            var rae = it as ResolvableApiException
                            rae.startResolutionForResult(activity, 0x1)
                        } catch (sie: IntentSender.SendIntentException) {
                            Log.i(TAG, "PendingIntent unable to execute request.")
                        }
                    }

                }
            }
    }
}
package com.walkly.walkly

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    // location service
    private var MY_PERMISSIONS_REQUEST_ACESS_FINE_LOCATION = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // start location service
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkPermission()
    }
    fun checkPermission(){
        // check if the location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e("Location_ERROR", "permission is not granted")
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_ACESS_FINE_LOCATION)
                // read location after getting permission
                readLocation()
            }
        }
        // permission is already granted, read location
        else {
            readLocation()
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

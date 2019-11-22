package com.walkly.walkly

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.walkly.walkly.utilities.DistanceUtil
import com.walkly.walkly.utilities.LocationUtil
import kotlinx.coroutines.CoroutineScope
import java.util.*

class MainActivity : AppCompatActivity(){

    private lateinit var locationUtil: LocationUtil
    private lateinit var distanceUtil: DistanceUtil

    private val cal = Calendar.getInstance()
    private val currentLocation = MutableLiveData<Location>()
    private val walkedDistance = MutableLiveData<Float>()

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

        locationUtil = LocationUtil(this, 100L, 50L)
        locationUtil.readLocation(currentLocation)

        currentLocation.observe(this, Observer {
            Log.d("Location_latitude", it?.latitude.toString() )
            Log.d("Location_longitude", it?.longitude.toString())
        })

        walkedDistance.observe(this, Observer {
            Log.d("Distance_walked", it.toString())
        })

        cal.add(Calendar.MINUTE, -1000)
        distanceUtil = DistanceUtil(this, cal.timeInMillis, 500, walkedDistance)

    }


    override fun onPause() {
        super.onPause()
        locationUtil.stopLocationUpdates()
        distanceUtil.startUpdates()
    }

    override fun onResume() {
        super.onResume()
        locationUtil.startLocationUpdates()
        distanceUtil.startUpdates()
    }
}

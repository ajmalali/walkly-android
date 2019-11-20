package com.walkly.walkly

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.walkly.walkly.utilities.DistanceUtil
import com.walkly.walkly.utilities.LocationUtil
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var locationUtil: LocationUtil
    private lateinit var distanceUtil: DistanceUtil

    val cal = Calendar.getInstance()

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

        locationUtil = LocationUtil(this)
        locationUtil.readLocation()

        distanceUtil = DistanceUtil(this)
        cal.add(Calendar.MINUTE, -1000)
        distanceUtil.getDistanceSince(cal.timeInMillis)

    }

}

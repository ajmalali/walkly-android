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
import com.google.firebase.auth.FirebaseAuth
import com.walkly.walkly.models.Player
import com.walkly.walkly.utilities.DistanceUtil
import com.walkly.walkly.utilities.LocationUtil
import java.util.*

class MainActivity : AppCompatActivity(){

    private lateinit var locationUtil: LocationUtil
    private lateinit var distanceUtil: DistanceUtil
    private lateinit var player: Player

    private val cal = Calendar.getInstance()

    private val currentLocation = MutableLiveData<Location>()
    private val walkedDistance = MutableLiveData<Float>()
    private val stamina = MutableLiveData<Long>()

    private val auth = FirebaseAuth.getInstance()

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
        supportActionBar?.hide()
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

        // the player model should not be initialized before valid sign in
        // the authentication activity shall not has this code to avoid auth checking in if statements

        if (auth.currentUser != null){
            player = Player(stamina)
            stamina.observe(this, Observer {stamina ->
                Log.d("Stamina: ", stamina.toString())
            })
        } else {
            auth.addAuthStateListener {
                if (it.currentUser != null){
                    player = Player(stamina)
                    stamina.observe(this, Observer {stamina ->
                        Log.d("Stamina: ", stamina.toString())
                    })
                    player.startStaminaUpdates()
                }
            }
        }

    }


    override fun onPause() {
        super.onPause()
        locationUtil.stopLocationUpdates()
        distanceUtil.stopUpdates()

        if (auth.currentUser != null)
            player.stopStaminaUpdates()
    }

    override fun onStop() {
        super.onStop()

        if (auth.currentUser != null)
            player.syncModel()
    }

    override fun onResume() {
        super.onResume()
        locationUtil.startLocationUpdates()
        distanceUtil.startUpdates()

        if (auth.currentUser != null)
            player.startStaminaUpdates()
    }
}

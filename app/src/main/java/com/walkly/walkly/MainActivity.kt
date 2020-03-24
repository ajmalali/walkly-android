package com.walkly.walkly

import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.walkly.walkly.models.Equipment
import com.walkly.walkly.models.Player
import com.walkly.walkly.utilities.DistanceUtil
import com.walkly.walkly.utilities.LocationUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_layout.*
import java.util.*

class MainActivity : AppCompatActivity(){

    // nav bar colors
    val SOLID_WHITE = Color.parseColor("#FFFFFF")
    val WHITE = Color.parseColor("#8AFFFFFF")

    private val cal = Calendar.getInstance()

    private val walkedDistance = MutableLiveData<Float>()
    val stamina = Player.stamina

    private val auth = FirebaseAuth.getInstance()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    //    val navView: BottomNavigationView = findViewById(R.id.nav_view)

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
    //    navView.setupWithNavController(navController)

        // TODO: refactor this
        // bottom nav
        btn_profile.setOnClickListener {
            navController.navigate(R.id.navigation_profile)
            // set this button to solid white color
            btn_profile.setTextColor(SOLID_WHITE)
            btn_profile.compoundDrawableTintList = ColorStateList.valueOf(SOLID_WHITE)
            // set other colors to half transparent white
            btn_map.setTextColor(WHITE)
            btn_map.compoundDrawableTintList = ColorStateList.valueOf(WHITE)
            btn_battles.setTextColor(WHITE)
            btn_battles.compoundDrawableTintList = ColorStateList.valueOf(WHITE)
        }
        btn_map.setOnClickListener {
            navController.navigate(R.id.navigation_map)
            // set this button to solid white color
            btn_map.setTextColor(SOLID_WHITE)
            btn_map.compoundDrawableTintList = ColorStateList.valueOf(SOLID_WHITE)
            // set other colors to half transparent white
            btn_profile.setTextColor(WHITE)
            btn_profile.compoundDrawableTintList = ColorStateList.valueOf(WHITE)
            btn_battles.setTextColor(WHITE)
            btn_battles.compoundDrawableTintList = ColorStateList.valueOf(WHITE)
        }
        btn_battles.setOnClickListener {
            navController.navigate(R.id.navigation_battles)
            // set this button to solid white color
            btn_battles.setTextColor(SOLID_WHITE)
            btn_battles.compoundDrawableTintList = ColorStateList.valueOf(SOLID_WHITE)
            // set other colors to half transparent white
            btn_profile.setTextColor(WHITE)
            btn_profile.compoundDrawableTintList = ColorStateList.valueOf(WHITE)
            btn_map.setTextColor(WHITE)
            btn_map.compoundDrawableTintList = ColorStateList.valueOf(WHITE)
        }
        // because the map is the main fragment
        btn_map.setTextColor(SOLID_WHITE)
        btn_map.compoundDrawableTintList = ColorStateList.valueOf(SOLID_WHITE)


        walkedDistance.observe(this, Observer {
            Log.d("Distance_walked steps", it.toString())
            Toast.makeText(this, "steps are $it", Toast.LENGTH_LONG).show()
        })

        cal.add(Calendar.MINUTE, -1000)

        // the player model should not be initialized before valid sign in
        // the authentication activity shall not has this code to avoid auth checking in if statements

        if (auth.currentUser != null){

            stamina.observe(this, Observer {stamina ->
                Log.d("Stamina: ", stamina.toString())
            })


        } else {
            auth.addAuthStateListener {
                if (it.currentUser != null){

                    stamina.observe(this, Observer {stamina ->
                        Log.d("Stamina: ", stamina.toString())
                    })
                    Player.startStaminaUpdates()
                }
            }
        }

        Player.level.observe(this, Observer {
            user_level.text = "LEVEL $it"
        })

        Player.progress.observe(this, Observer {
            progressBar2.progress = it.toInt()
        })

        updateTopBar()

        // TODO: if connected to internet cache rewards locally

    }


    override fun onStop() {
        super.onStop()

        if (auth.currentUser != null)
            Player.stopStaminaUpdates()

        if (auth.currentUser != null)
            Player.syncModel()
    }


    override fun onStart() {
        super.onStart()

        if (auth.currentUser != null)
            Player.startStaminaUpdates()
    }

    fun updateTopBar(){
        Player.stamina.observe(this, Observer {
            Log.d("stamina from map2", it.toString())

            join_button.isClickable = true
            join_button.background.alpha = 255

            if(it >= 300){
                //3 balls
                stamina1full.visibility = View.VISIBLE
                stamina2full.visibility = View.VISIBLE
                stamina3full.visibility = View.VISIBLE

            }else if(it >= 200 ){
                //2 balls
                stamina1full.visibility = View.VISIBLE
                stamina2full.visibility = View.VISIBLE
                stamina3full.visibility = View.INVISIBLE

            }else if(it >= 100){
                //1 ball
                stamina1full.visibility = View.VISIBLE
                stamina2full.visibility = View.INVISIBLE
                stamina3full.visibility = View.INVISIBLE

            }else{
                //no balls
                stamina1full.visibility = View.INVISIBLE
                stamina2full.visibility = View.INVISIBLE
                stamina3full.visibility = View.INVISIBLE

                // player cannot join a battle
                join_button.isClickable = false
                join_button.background.alpha = 100
            }

        })
    }
}

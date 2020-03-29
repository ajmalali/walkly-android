package com.walkly.walkly

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.walkly.walkly.auth.LoginActivity
import com.walkly.walkly.models.Player
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_layout.*
import kotlinx.android.synthetic.main.dialog_feedback.*
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

        menu.setOnClickListener {
            drawer_layout.open()
        }

        val navController = findNavController(R.id.nav_host_fragment)
        nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_settings -> {
                    navController.navigate(R.id.accountSettingsFragment)
                    drawer_layout.close()
                    return@setNavigationItemSelectedListener true
                }
                R.id.nav_send_feedback -> {
                    val feedbackDialog = AlertDialog.Builder(this)
                        .setView(R.layout.dialog_feedback)
                        .create()
                    feedbackDialog.show()
                    feedbackDialog.findViewById<Button>(R.id.btn_cancel)
                        ?.setOnClickListener {
                            feedbackDialog.dismiss()
                        }
                    feedbackDialog.findViewById<Button>(R.id.btn_send)
                        ?.setOnClickListener {
                            val title = feedbackDialog.findViewById<EditText>(R.id.et_title)?.text.toString()
                            val content = feedbackDialog.findViewById<EditText>(R.id.et_content)?.text.toString()
                            FirebaseFirestore.getInstance().collection("feedbacks")
                                .add(hashMapOf(
                                    "title" to title,
                                    "content" to content,
                                    "timestamp" to FieldValue.serverTimestamp(),
                                    "userID" to auth.currentUser?.uid,
                                    "closed" to false
                                ))
                                .addOnSuccessListener {
                                    feedbackDialog.dismiss()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Failed to send feedback. Check your connection", Toast.LENGTH_LONG)
                                }
                        }

                    drawer_layout.close()
                }
                R.id.nav_logout -> {
                    signOut()
                }
            }
            return@setNavigationItemSelectedListener false
        }

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
            progressBar.progress = it.toInt()
        })

        updateTopBar()

        // TODO: if connected to internet cache rewards locally

    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        this?.finish()
    }

    override fun onBackPressed() {
        if (drawer_layout.isOpen)
            drawer_layout.close()
        else
            super.onBackPressed()
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
                view_energy_ball_1.alpha = 1f
                view_energy_ball_2.alpha = 1f
                view_energy_ball_3.alpha = 1f

            }else if(it >= 200 ){
                //2 balls
                view_energy_ball_1.alpha = 1f
                view_energy_ball_2.alpha = 1f
                view_energy_ball_3.alpha = 0.5f

            }else if(it >= 100){
                //1 ball
                view_energy_ball_1.alpha = 1f
                view_energy_ball_2.alpha = 0.5f
                view_energy_ball_3.alpha = 0.5f

            }else{
                //no balls
                view_energy_ball_1.alpha = 0.5f
                view_energy_ball_2.alpha = 0.5f
                view_energy_ball_3.alpha = 0.5f

                // player cannot join a battle
                join_button.isClickable = false
                join_button.background.alpha = 100
            }

        })
    }
}

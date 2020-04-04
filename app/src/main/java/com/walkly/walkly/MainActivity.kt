package com.walkly.walkly

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Observer
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.iid.FirebaseInstanceId
import com.walkly.walkly.auth.LoginActivity
import com.walkly.walkly.models.Feedback
import com.walkly.walkly.repositories.ConsumablesRepository
import com.walkly.walkly.repositories.EquipmentRepository
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_layout.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.util.*

// max of 3 stamina points
private const val MAX_STAMINA = 300

// update every 36 seconds (idk why 36)
private const val INTERVAL = 36000L

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    // nav bar colors
    private val SOLID_WHITE = Color.parseColor("#FFFFFF")
    private val WHITE = Color.parseColor("#8AFFFFFF")

    private val db = FirebaseFirestore.getInstance()
    private val currentPlayer = PlayerRepository.getPlayer()
    private val stamina = MutableLiveData<Long>()

    private var update = false
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private lateinit var feedbackDialog: AlertDialog
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
                            val title =
                                feedbackDialog.findViewById<EditText>(R.id.et_title)?.text.toString()
                            val content =
                                feedbackDialog.findViewById<EditText>(R.id.et_content)?.text.toString()
                            db.collection("feedbacks")
                                .add(
                                    hashMapOf(
                                        "title" to title,
                                        "content" to content,
                                        "timestamp" to FieldValue.serverTimestamp(),
                                        "userID" to auth.currentUser?.uid,
                                        "closed" to false
                                    )
                                )
                                .addOnSuccessListener {
                                    feedbackDialog.dismiss()
                                    Toast.makeText(
                                        baseContext, "Thank you for your feedback!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "Failed to send feedback. Check your connection",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
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

        updateTopBar()

        when {
            intent.extras?.getString("target-fragment") == "friend-list" -> {
                navController.navigate(R.id.friendsFragment)
            }
        }
    }

    private fun updateTopBar() {
        stamina.observe(this, Observer {
            val stamina = it
            join_button.isClickable = true
            join_button.background.alpha = 255

            stamina.let {
                if (stamina <= 100) {
                    // no balls
                    view_energy_ball_3.visibility = View.INVISIBLE
                    view_energy_ball_2.visibility = View.INVISIBLE
                    view_energy_ball_1.visibility = View.INVISIBLE

                    // player cannot join a battle
                    join_button.isClickable = false
                    join_button.background.alpha = 100
                } else {
                    if (stamina >= 300) {
                        view_energy_ball_3.visibility = View.VISIBLE
                    }

                    if (stamina >= 200) {
                        view_energy_ball_2.visibility = View.VISIBLE
                    }

                    if (stamina >= 100) {
                        view_energy_ball_1.visibility = View.VISIBLE
                    }
                }
            }

            Log.d("Stamina from map", stamina.toString())
        })
    }

    // TODO: (UI) Change to snackbar
    private suspend fun displayMessage(message: String?) {
        withContext(Dispatchers.Main) {
            // If sign in fails, display a message to the user.
            Log.d(TAG, "Error occurred")
            Toast.makeText(
                baseContext, message ?: "No error message",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onStart() {
        super.onStart()
        // the player model should not be initialized before valid sign in
        // the authentication activity shall not has this code to avoid auth checking in if statements

        // TODO: if connected to internet cache rewards locally
        auth.addAuthStateListener {
            it.currentUser?.let {
                startStaminaUpdates()
            }
        }
    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    override fun onBackPressed() {
        if (drawer_layout.isOpen)
            drawer_layout.close()
        else
            super.onBackPressed()
    }


    // TODO: Syncing happens here
    // Sync everything to DB before closing
    override fun onStop() {
        super.onStop()
        auth.currentUser?.let {
            CoroutineScope(IO).launch {
                try {
                    stopStaminaUpdates()
                    PlayerRepository.syncPlayer()
                    ConsumablesRepository.syncConsumables()
                    EquipmentRepository.syncEquipment()
                } catch (e: FirebaseFirestoreException) {
                    displayMessage(e.message)
                } catch (e: Exception) {
                    displayMessage(e.message)
                }
            }

        }
    }

    private fun stopStaminaUpdates() {
        update = false
    }

    // Increases the stamina of the current player every 36 seconds
    private fun startStaminaUpdates() {
        update = true
        scope.launch {
            while (update && currentPlayer.stamina?.compareTo(MAX_STAMINA)!! < 0) {
                delay(INTERVAL)
                currentPlayer.stamina = currentPlayer.stamina?.inc()
                stamina.postValue(currentPlayer.stamina)
                Log.d(TAG, "Current stamina: ${currentPlayer.stamina}")
            }
        }
    }
}

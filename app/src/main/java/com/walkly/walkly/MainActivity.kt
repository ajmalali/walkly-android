package com.walkly.walkly

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.walkly.walkly.repositories.ConsumablesRepository
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.android.synthetic.main.activity_main.*
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

    private val cal = Calendar.getInstance()

    private val walkedDistance = MutableLiveData<Float>()

    private val currentPlayer = PlayerRepository.getPlayer()
    private val stamina = currentPlayer.stamina

    private var update = false
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

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

        cal.add(Calendar.MINUTE, -1000)
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

    // TODO: Syncing happens here
    // Sync player to DB before closing
    override fun onStop() {
        super.onStop()
        auth.currentUser?.let {
            CoroutineScope(IO).launch {
                try {
                    stopStaminaUpdates()
                    PlayerRepository.syncPlayer()
                    ConsumablesRepository.syncConsumables()
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
                Log.d(TAG, "Current stamina: ${currentPlayer.stamina}")
            }
        }
    }


}

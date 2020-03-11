package com.walkly.walkly.offlineBattle

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.SensorsClient
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.OnDataPointListener
import com.walkly.walkly.R
import com.walkly.walkly.utilities.DistanceUtil

class BackgroundOfflineBattleService : Service() {

    private lateinit var sensorsClient: SensorsClient
    private lateinit var info: OfflineBattleViewModel.OfflineServiceInfo
    private var steps = 0
    private val listener = OnDataPointListener {
        val value = it?.getValue(Field.FIELD_STEPS)?.asInt()
        Log.d(TAG, "data point => $value")
        if (value != null) {
            steps += value * 5 // multiplying by 5 just for testing
            // player wins
            if ((steps * info.playerPower) >= info.enemyHealth){
                Log.d(TAG, "player won")
                NotificationManagerCompat.from(this)
                    .notify(1, notifyBuilder.build())
                // TODO: stop service
                // TODO: clicking notification take user to battle activity
                //      with a suitable end battle dialog
            }

        }
    }
    private val notifyBuilder = NotificationCompat.Builder(this,  CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Battle has ended")
        .setContentText("Congrats you won! stay healthy everyday")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    override fun onCreate() {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_ID
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "background service started")

        info = intent?.getSerializableExtra("info") as OfflineBattleViewModel.OfflineServiceInfo

        val googleSignInAccount = GoogleSignIn.getAccountForExtension(
            this, DistanceUtil.stepsFitnessOptions)
        sensorsClient = Fitness.getSensorsClient(this, googleSignInAccount).also {
            it.add(DistanceUtil.request, listener)
                .addOnSuccessListener {
                    Log.d(TAG, "steps listener registered")
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "steps listener failed", e)
                }
        }

        // TODO damaging player logic
        // TODO notify player on lose

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "background service destroyed")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object{
        private const val TAG = "Offline_Battle_Service"
        private const val CHANNEL_ID = "battle status"
    }
}

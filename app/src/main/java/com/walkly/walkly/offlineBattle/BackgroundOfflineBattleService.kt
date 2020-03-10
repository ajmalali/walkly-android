package com.walkly.walkly.offlineBattle

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.SensorsClient
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.OnDataPointListener
import com.walkly.walkly.utilities.DistanceUtil

class BackgroundOfflineBattleService : Service() {

    private lateinit var sensorsClient: SensorsClient
    private lateinit var info: OfflineBattleViewModel.OfflineServiceInfo
    private var steps = 0
    private val listener = OnDataPointListener {
        val value = it?.getValue(Field.FIELD_STEPS)?.asInt()
        if (value != null) {
            steps += value
        }
        Log.d(TAG, "data point => $value")
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
    }
}

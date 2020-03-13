package com.walkly.walkly.utilities

import android.Manifest
import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import java.util.concurrent.TimeUnit


class DistanceUtil(
    private val activity: Activity,
    private val data: MutableLiveData<Float>
) {
    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(activity).and(0xFFF)

    private val googleSignInAccount =
        GoogleSignIn.getAccountForExtension(activity, stepsFitnessOptions)

    companion object {
        val stepsFitnessOptions: FitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA)
            .build()
        val request: SensorRequest = SensorRequest.Builder()
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setSamplingRate(1000, TimeUnit.MILLISECONDS)
            .build()
    }

    init {
        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(activity),
                stepsFitnessOptions
            )
        ) {
            GoogleSignIn.requestPermissions(
                activity,
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                GoogleSignIn.getLastSignedInAccount(activity),
                stepsFitnessOptions
            )
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE);
        }
        getStepsSince()
    }

    private fun getStepsSince() {
        Log.d("Distance", "starting updates")

        val listener = OnDataPointListener { dataPoint ->
            Log.d("DistanceUtil", "onDataPoint")
            val value = dataPoint?.getValue(Field.FIELD_STEPS)?.asInt()
            data.value = value?.toFloat()
        }

        Fitness.getSensorsClient(activity, googleSignInAccount)
            .add(request, listener)
            .addOnSuccessListener {
                Log.d("DistanceUtil", "steps listener registered")
            }
            .addOnFailureListener {
                Log.d("DistanceUtil", "steps listener failed", it)
            }

    }
}
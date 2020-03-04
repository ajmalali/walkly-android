package com.walkly.walkly.utilities

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import com.google.android.gms.fitness.result.DataReadResponse
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit

class DistanceUtil(
    private val activity: Activity,
    private val data: MutableLiveData<Float>
) {
    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(activity).and(0xFFF)

    private val stepsFitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA)
        .build()

    private val googleSignInAccount =
        GoogleSignIn.getAccountForExtension(activity, stepsFitnessOptions)

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
        }
        getStepsSince()
    }

    private fun getStepsSince() {
        Log.d("Distance", "starting updates")
        val request = SensorRequest.Builder()
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setSamplingRate(1000, TimeUnit.MILLISECONDS)
            .build()

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
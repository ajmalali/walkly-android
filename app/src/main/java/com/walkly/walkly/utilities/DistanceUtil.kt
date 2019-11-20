package com.walkly.walkly.utilities

import android.app.Activity
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import java.util.*
import java.util.concurrent.TimeUnit

class DistanceUtil (activity: Activity){
    private val activity = activity
    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(activity).and(0xFFF)
    private val REQUEST_TAG = "Request status"
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .build()
    var lastRead = 0L

    init {
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity), fitnessOptions)){
            GoogleSignIn.requestPermissions(
                activity,
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                GoogleSignIn.getLastSignedInAccount(activity),
                fitnessOptions
            )
        }
    }

    fun getDistanceSince(startTime: Long) {
        var endTime = Calendar.getInstance().timeInMillis
        Log.d("start time: ", startTime.toString())
        Log.d("end time: ", endTime.toString())
        var distance = 0F
        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()
            GoogleSignIn.getLastSignedInAccount(activity)?.let {
                Fitness.getHistoryClient(activity, it)
                    .readData(readRequest)
                    .addOnSuccessListener {
                        Log.d(REQUEST_TAG, "SUCCESS")
                        floatDistance(it)
                    }
                    .addOnFailureListener {
                        Log.e(REQUEST_TAG, "FAILURE")
                    }
                    .addOnCompleteListener {
                        Log.d(REQUEST_TAG, "COMPLETE")
                        Log.d(REQUEST_TAG, endTime.toString())
                        lastRead = endTime
                        Log.d(REQUEST_TAG, lastRead.toString())
                    }
            Log.d("distance: ", distance.toString())
            Thread.sleep(500)
            Log.d("start time: ", startTime.toString())
            Log.d("end time: ", endTime.toString())
            endTime = Calendar.getInstance().timeInMillis
        }

    }

    private fun floatDistance(response: DataReadResponse?){
        var distance = 0F
        response?.buckets.let { buckets ->
            for (bucket in buckets!!){
                for (dataSet in bucket.dataSets){
                    for (dp in dataSet.dataPoints){
                        distance.plus(dp.getValue(Field.FIELD_DISTANCE).asFloat())
                        Log.d("value", dp.getValue(Field.FIELD_DISTANCE).asFloat().toString())
                    }
                }
            }
        }
    }
}
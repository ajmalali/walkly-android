package com.walkly.walkly.utilities

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class DistanceUtil (activity: Activity, startTime: Long, interval: Long, data: MutableLiveData<Float>) {
    private val activity = activity
    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(activity).and(0xFFF)
    private val REQUEST_TAG = "Request status"
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .build()

    var lastRead = 0L

    private var update = false
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private val startTime = startTime
    private val interval = interval
    private val data = data

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

    fun stopUpdates(){
        update = false
    }
    fun startUpdates(){
        update = true
        scope.launch {
            Log.d("Distance", "launching coroutine")
            getDistanceSince()
        }
    }

    private suspend fun getDistanceSince() {
        var endTime: Long

        while (update){
            endTime = Calendar.getInstance().timeInMillis
            Log.d("start time: ", startTime.toString())
            Log.d("end time: ", endTime.toString())

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
            }
            delay(interval)
        }


    }

    private fun floatDistance(response: DataReadResponse?){
        response?.buckets.let { buckets ->
            for (bucket in buckets!!){
                for (dataSet in bucket.dataSets){
                    for (dp in dataSet.dataPoints){
                        val value = dp.getValue(Field.FIELD_DISTANCE).asFloat()
                        Log.d("value", value.toString())
                        data.value = value
                    }
                }
            }
        }
    }

}
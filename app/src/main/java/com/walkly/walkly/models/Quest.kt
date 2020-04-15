package com.walkly.walkly.models

import android.location.Location
import com.google.firebase.firestore.GeoPoint

data class Quest(
    val name: String = "",
    val hint: String = "",
    val location: GeoPoint? = null
){
    lateinit var id : String
    var distance: Int = 0
    var closeEnough = false

    fun addId(id: String): Quest{
        this.id = id
        return this
    }

    fun calculateDistance(cLocation: Location) {
        if (location != null) {
            val qLocation = Location("quest").apply {
                latitude = location.latitude
                longitude = location.longitude
            }
            distance =  qLocation.distanceTo(cLocation).toInt()
            closeEnough = distance <= 50
        }
    }
}

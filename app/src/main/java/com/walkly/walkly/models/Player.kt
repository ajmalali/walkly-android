package com.walkly.walkly.models

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FirebaseFirestoreException
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.floor

private const val TAG = "class Player"

@Parcelize
data class Player(
    var name: String? = "2Lazy4u",
    var email: String? = "lazy@email.com",
    var currentEquipment: Equipment? = null,
    var currentHP: Long? = 100L,
    var level: Long? = 1,
    var stamina: Long? = 300L,
    var points: Long? = 0,
    var progress: Long? = 0,
    var lastUpdate: String? = null,
    var photoURL: String? = null,
    var deviceToken: String = ""
): Parcelable {

    @IgnoredOnParcel
    @get:Exclude var id: String? = null

    fun joinBattle() {
        stamina = stamina?.minus(100)
    }

    fun wearEquipment(equipment: Equipment) {
        currentEquipment = equipment
    }

    // TODO: calculate progress form points
    // to save computation time calculate it and store it every time points change
}
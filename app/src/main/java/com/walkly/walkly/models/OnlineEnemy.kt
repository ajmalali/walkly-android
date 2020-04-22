package com.walkly.walkly.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class OnlineEnemy(
    val name: String? = "",
    val image: String? = "",
    val health: Long = 100,
    val damage: Long = 1,
    val level: Long = 1,
    val reward: List<Equipment>? = null
) : Parcelable {
    var id: String = ""

}


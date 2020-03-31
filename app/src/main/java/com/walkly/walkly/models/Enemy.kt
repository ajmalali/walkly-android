package com.walkly.walkly.models

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Enemy(
    var name: String? = "",
    var level: Long? = 1,
    var image: String? = "",
    var health: Long? = 100,
    var damage: Long? = 1
) : Parcelable {
    @IgnoredOnParcel
    var id: String = ""

    fun addId(value: String): Enemy {
        this.id = value
        return this
    }
}


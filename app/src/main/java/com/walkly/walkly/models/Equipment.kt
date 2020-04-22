package com.walkly.walkly.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Equipment(
    var name: String? = "",
    var level: Long? = 0,
    var type: String? = "",
    var image: String? = "",
    var value: Long? = 0
) : Parcelable {

    @IgnoredOnParcel
    var id: String? = null

    fun addId(value: String): Equipment {
        this.id = value
        return this
    }
}

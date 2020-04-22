package com.walkly.walkly.models

import android.os.Parcelable
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

/* TODO
*   - user has name
*   - friend list is array
*   - user has image url*/

@Parcelize
data class Friend(
    val name: String = "",
    val level: Int = 0,
    val points: Int = 0,
    var photoURL: String = "",
    var friends: List<String> = listOf()
) : Parcelable {
    @IgnoredOnParcel
    lateinit var id: String
    @IgnoredOnParcel
    lateinit var status: String
    @IgnoredOnParcel
    var type = -1

    fun addIdAndStatus(id: String, status: String): Friend {
        this.id = id
        this.status = status
        return this
    }

    fun addId(id: String): Friend {
        this.id = id
        return this
    }
}
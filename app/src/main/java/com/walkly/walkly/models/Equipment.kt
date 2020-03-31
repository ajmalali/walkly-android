package com.walkly.walkly.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.Exclude

data class Equipment(
    var name: String? = "",
    var level: Long? = 0,
    var type: String? = "",
    var image: String? = "",
    var value: Long? = 0
) : Parcelable {

    var id: String? = null

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long
    ) {
        id = parcel.readString()
    }

    fun addId(value: String): Equipment {
        this.id = value
        return this
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeValue(level)
        parcel.writeString(type)
        parcel.writeString(image)
        parcel.writeValue(value)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Equipment> {
        override fun createFromParcel(parcel: Parcel): Equipment {
            return Equipment(parcel)
        }

        override fun newArray(size: Int): Array<Equipment?> {
            return arrayOfNulls(size)
        }
    }
}

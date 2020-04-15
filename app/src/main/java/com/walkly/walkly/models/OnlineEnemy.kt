package com.walkly.walkly.models

import android.os.Parcel
import android.os.Parcelable

class OnlineEnemy(
    val name: String? = "",
    val image: String? = "",
    val health: Long = 100,
    val damage: Long = 1,
    val level: Long = 1,
    val reward: List<Equipment>? = null
): Parcelable {
    var id: String = ""

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.createTypedArrayList(Equipment)
    ) {
        id = parcel.readString()!!
    }

    fun addId(value: String): OnlineEnemy {
        this.id = value
        return this
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(image)
        parcel.writeLong(health)
        parcel.writeLong(damage)
        parcel.writeLong(level)
        parcel.writeTypedList(reward)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OnlineEnemy> {
        override fun createFromParcel(parcel: Parcel): OnlineEnemy {
            return OnlineEnemy(parcel)
        }

        override fun newArray(size: Int): Array<OnlineEnemy?> {
            return arrayOfNulls(size)
        }
    }
}


package com.walkly.walkly.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BattlePlayer(
    var id: String = "",
    var name: String = "",
    var avatarURL: String = "",
    var equipmentURL: String = ""
) : Parcelable

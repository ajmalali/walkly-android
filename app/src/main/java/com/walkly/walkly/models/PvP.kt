package com.walkly.walkly.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PvP(
    var id: String? = "",
    var hostName: String? = "",
    var hostImage: String? = "",
    var hostEquipmentImage: String? = "",
    var opponentName: String? = "",
    var opponentImage: String? = "",
    var opponentEquipmentImage: String? = "",
    var hostHealth: Int? = 100, // TODO: hardcoded
    var opponentHealth: Int? = 100 // TODO: hardcoded
) : Parcelable
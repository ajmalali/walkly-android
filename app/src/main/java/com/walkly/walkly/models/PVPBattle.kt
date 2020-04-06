package com.walkly.walkly.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PVPBattle(
    var id: String = "",
    var host: BattlePlayer? = null,
    var opponent: BattlePlayer? = null
) : Parcelable
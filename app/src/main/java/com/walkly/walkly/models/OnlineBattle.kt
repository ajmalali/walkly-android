package com.walkly.walkly.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OnlineBattle(
    var battleName: String? = "",
    var battleState: String? = "In-lobby",
    var playerCount: Int? = 1,
    var hostName: String? = "",
    var enemy: Enemy? = null,
    var enemyHealth: Long? = 100,
    var combinedPlayersHealth: Int? = 100,
    var players: MutableList<BattlePlayer> = mutableListOf(),
    var id: String? = ""
) : Parcelable
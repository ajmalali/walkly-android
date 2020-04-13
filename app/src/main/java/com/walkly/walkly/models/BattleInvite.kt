package com.walkly.walkly.models

data class BattleInvite(
    var battleID: String = "",
    var type: String = "", // PVP or Online Battle
    var hostName: String = "",
    var toIDs: MutableList<String> = mutableListOf()
)

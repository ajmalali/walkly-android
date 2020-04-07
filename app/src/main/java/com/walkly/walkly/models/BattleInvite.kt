package com.walkly.walkly.models

data class BattleInvite(
    var battleID: String = "",
    var type: String = "", // PVP or online battle
    var fromName: String = "",
    var toIDs: MutableList<String> = mutableListOf()
)

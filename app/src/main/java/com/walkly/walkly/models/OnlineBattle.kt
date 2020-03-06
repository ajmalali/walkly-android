package com.walkly.walkly.models


// TODO: use Parecelable interface to easily send to activity
data class OnlineBattle(
    val enemy: Enemy? = null,
    val enemyHealth: Int = 1000,
    val players: List<Player>
) {


}
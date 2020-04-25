package com.walkly.walkly.ui.leaderboard

// Data class to represent entries in the leaderboard
data class LeaderboardItem(
    val name: String = "",
    val level: Int = 0,
    val points: Int = 0,
    val photoURL: String = ""
) : Comparable<LeaderboardItem> {
    lateinit var id: String

    // Used to add ID of a user and using document.toObject method
    fun addId(value: String): LeaderboardItem {
        this.id = value
        return this
    }

    // Compare to sort in descending order
    override fun compareTo(other: LeaderboardItem): Int {
        return other.points - this.points
    }
}
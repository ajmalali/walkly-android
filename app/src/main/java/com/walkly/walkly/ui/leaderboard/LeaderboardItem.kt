package com.walkly.walkly.ui.leaderboard

// Data class to represent entries in the leaderboard
data class LeaderboardItem(val name: String = "", val level: Int = 0, val points: Int = 0) : Comparable<LeaderboardItem> {
    lateinit var id: String

    // Used to add ID of a user and using document.toObject method
    fun addId(value: String): LeaderboardItem {
        this.id = value
        return this
    }

    override fun compareTo(other: LeaderboardItem): Int {
        return this.points - other.points
    }
}
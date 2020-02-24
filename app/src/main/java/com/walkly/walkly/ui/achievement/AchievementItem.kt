package com.walkly.walkly.ui.achievement

// Data class to represent entries in the achievement
data class AchievementItem(val name: String = "", val level: Int = 0, val points: Int = 0) : Comparable<AchievementItem> {
    lateinit var id: String

    // Used to add ID of a user and using document.toObject method
    fun addId(value: String): AchievementItem {
        this.id = value
        return this
    }

    // Compare to sort in descending order
    override fun compareTo(other: AchievementItem): Int {
        return other.points - this.points
    }
}
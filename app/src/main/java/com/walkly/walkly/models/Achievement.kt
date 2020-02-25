package com.walkly.walkly.models

data class Achievement(
    val name: String = "",
    val level: Int = 0,
    val image: String = "",
    val points: Int = 0
) {
    lateinit var id: String

    // Used to add ID of a user and using document.toObject method
    fun addId(value: String): Achievement {
        this.id = value
        return this
    }
}
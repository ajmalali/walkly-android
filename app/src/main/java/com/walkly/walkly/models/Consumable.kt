package com.walkly.walkly.models

data class Consumable(
    val name: String = "",
    val level: Int = 0,
    val image: String = "",
    val type: String = "",
    val value: Int = 0
) {
    lateinit var id: String

    // Used to add ID of a user and using document.toObject method
    fun addId(value: String): Consumable {
        this.id = value
        return this
    }
}
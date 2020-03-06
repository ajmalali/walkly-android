package com.walkly.walkly.models

class Battle(val battleName: String, val playerCount: Int, val host: String) {
    lateinit var id: String

    fun addId(value: String): Battle {
        this.id = value
        return this
    }
}
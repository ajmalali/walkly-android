package com.walkly.walkly.models

data class Friend (val name: String = "", val level: Int = 0, val points: Int = 0) {
    lateinit var id: String

    fun addId(value: String): Friend{
        this.id = value
        return this
    }
}
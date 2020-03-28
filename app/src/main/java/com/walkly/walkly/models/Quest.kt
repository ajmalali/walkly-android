package com.walkly.walkly.models

data class Quest(
    val name: String = "",
    val distance: Int = 0,
    val hint: String = ""
){
    lateinit var id : String

    fun addId(id: String): Quest{
        this.id = id
        return this
    }
}

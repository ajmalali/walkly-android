package com.walkly.walkly.models

data class Friend (val name: String = "", val level: Int = 0, val points: Int = 0, var status: String ="") {
    lateinit var id: String
    lateinit var img: String

    fun addIdAndStatus(id: String, status: String): Friend{
        this.id = id
        this.status = status
        return this
    }

    fun addImage(url: String){
        this.img = url
    }
}
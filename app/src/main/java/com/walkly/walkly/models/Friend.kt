package com.walkly.walkly.models

import com.google.firebase.auth.FirebaseAuth

/* TODO
*   - user has name
*   - friend list is array
*   - user has image url*/

data class Friend (
    val name: String = "",
    val level: Int = 0,
    val points: Int = 0,
    var photoURL: String = "",
    var friends: List<String> = listOf()
) {
    lateinit var id: String
    lateinit var status: String
    var type = -1

    fun addIdAndStatus(id: String, status: String): Friend{
        this.id = id
        this.status = status
        return this
    }

    fun addId(id: String): Friend {
        this.id = id
        return this
    }
}
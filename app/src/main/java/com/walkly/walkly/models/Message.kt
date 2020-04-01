package com.walkly.walkly.models

import com.google.firebase.Timestamp
import java.util.*

data class Message(
    val text: String = "",
    val time: Timestamp? = null
) : Comparable<Message>{
    fun getTime() : String{
        val cal = Calendar.getInstance(TimeZone.getDefault()).apply {
            timeInMillis = time.time
        }
        return android.text.format.DateFormat.format("HH:MM", cal).toString()
    }

    override fun compareTo(other: Message): Int {
        return this.time?.compareTo(other?.time as Timestamp) as Int
    }

}

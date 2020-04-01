package com.walkly.walkly.models

import android.util.Log
import com.google.firebase.Timestamp
import java.util.*

private const val TAG = "Message_Model"

data class Message(
    val text: String = "",
    val time: Timestamp? = null
) : Comparable<Message>{

    lateinit var avatar: String
    var sent: Boolean = false

    fun addAvatar(src: String) : Message{
        this.avatar = src
        return this
    }

    fun stringTime() : String {
        val cal = Calendar.getInstance(TimeZone.getDefault()).apply {
            timeInMillis = time.time
            Log.d(TAG, "time in mills is $timeInMillis")
        }
        return android.text.format.DateFormat.format("HH:MM", cal).toString()
    }

    override fun compareTo(other: Message): Int {
        return try {
            this.time?.compareTo(other.time as Timestamp) as Int
        } catch (tce: TypeCastException){
            1
        }
    }

}

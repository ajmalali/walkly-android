package com.walkly.walkly.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.walkly.walkly.R
import com.walkly.walkly.models.Message
import kotlinx.android.synthetic.main.list_chat_message.view.*

class ChatAdapter() : RecyclerView.Adapter<MessageViewHolder>(){
    private lateinit var messages: List<Message>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    fun setMessages(messages: List<Message>){
        this.messages = messages
            notifyDataSetChanged()
    }
}

class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view){
    private val msgText: TextView = view.tv_message
    private val time: TextView = view.tv_time

    fun bind(message: Message){
        msgText.text = message.text
        time.text = message.getTime()
    }
}

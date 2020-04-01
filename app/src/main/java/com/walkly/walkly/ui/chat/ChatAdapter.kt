package com.walkly.walkly.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.walkly.walkly.R
import com.walkly.walkly.models.Message
import kotlinx.android.synthetic.main.list_chat_message.view.*

class ChatAdapter(var messages: List<Message>) : RecyclerView.Adapter<MessageViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    fun updateMessages(messages: List<Message>){
        this.messages = messages
        notifyDataSetChanged()
    }
}

class MessageViewHolder(val view: View) : RecyclerView.ViewHolder(view){
    private val msgText: TextView = view.tv_message
    private val time: TextView = view.tv_time
    private val avatar: ImageView = view.img_avatar

    fun bind(message: Message){
        msgText.text = message.text
        time.text = "received at ${message.stringTime()}"
        Glide.with(view)
            .load(message.avatar)
            .into(avatar)
    }
}

package com.walkly.walkly.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.walkly.walkly.R
import com.walkly.walkly.models.Message
import com.walkly.walkly.repositories.ChatRepository
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment : Fragment() {

    private lateinit var messages: List<Message>
    private var adapter: ChatAdapter = ChatAdapter()

    val args: ChatFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ChatRepository.getChat(args.friendId){
            this.messages = it
            adapter.setMessages(messages)
        }
        rv_chat.adapter = adapter
    }
}
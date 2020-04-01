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

    private var messages: List<Message> = emptyList()
    private var adapter: ChatAdapter = ChatAdapter(messages)

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
        chat_recycler_view.adapter = adapter
        ChatRepository.getChat(args.friendId){
            this.messages = it
            adapter.updateMessages(messages)
        }
    }
}
package com.walkly.walkly.ui.profile

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.walkly.walkly.R
import com.walkly.walkly.models.Friend
import kotlinx.android.synthetic.main.list_friend.view.img_avatar_1
import kotlinx.android.synthetic.main.list_friend.view.tv_level_1
import kotlinx.android.synthetic.main.list_friend.view.tv_username_1
import kotlinx.android.synthetic.main.list_invite_friend.view.*
import java.lang.Exception

private const val TAG = "InviteFriendAdapter"

class InviteFriendsAdapter(
    var friends: List<Friend>,
    private val listener: OnFriendInviteListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.list_invite_friend, parent, false)

        return InviteFriendViewHolder(
            view,
            listener
        )
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val friend = friends[position]
        (holder as BindibleViewHolder).bind(friend)
    }

    interface OnFriendInviteListener {
        fun onFriendInviteClick(position: Int)
    }
}

class InviteFriendViewHolder(
    view: View,
    private val listener: InviteFriendsAdapter.OnFriendInviteListener
) : RecyclerView.ViewHolder(view), BindibleViewHolder {
    private val name: TextView = view.tv_username_1
    private val level: TextView = view.tv_level_1
    private val avatar: ImageView = view.img_avatar_1
    private val inviteButton = view.btn_invite

    override fun bind(friend: Friend) {
        name.text = friend.name
        level.text = "Level ${friend.level}"
        try {
            Glide.with(this.itemView)
                .load(friend.photoURL)
                .into(avatar)
        } catch (e: Exception) {
            Log.d(TAG, "Failed to load avatar")
        }

        inviteButton.setOnClickListener {
            listener.onFriendInviteClick(adapterPosition)
            inviteButton.text = "Invited"
            inviteButton.isEnabled = false
            inviteButton.background.alpha = 100
        }
    }
}
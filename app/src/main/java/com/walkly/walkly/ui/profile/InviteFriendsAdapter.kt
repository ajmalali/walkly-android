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
import kotlinx.android.synthetic.main.list_friend.view.*
import java.lang.Exception

private const val TAG = "InviteFriendAdapter"


class InviteFriendsAdapter(var friends: List<Friend>, private val listener: OnFriendInviteListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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

    override fun getItemViewType(position: Int): Int {
        return friends[position].type
    }
    interface OnFriendInviteListener {
        fun onFriendInviteClick(position: Int)
    }
}

class InviteFriendViewHolder(view: View, private val listener: InviteFriendsAdapter.OnFriendInviteListener): RecyclerView.ViewHolder(view), BindibleViewHolder, View.OnClickListener  {
    private val name: TextView = view.tv_username_1
    private val level: TextView = view.tv_level_1
    private val avatar: ImageView = view.img_avatar_1

    override fun bind(friend: Friend){
        name.text = friend.name
        level.text = friend.level.toString()
        try {
            Glide.with(this.itemView)
                .load(friend.photoURL)
                .into(avatar)
        } catch (e: Exception){
            Log.d(TAG, "Failed to load avatar")
        }
    }

    override fun onClick(p0: View?) {
        listener.onFriendInviteClick(adapterPosition)
    }

}


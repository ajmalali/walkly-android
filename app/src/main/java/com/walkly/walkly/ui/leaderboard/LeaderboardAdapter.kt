package com.walkly.walkly.ui.leaderboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.walkly.walkly.R

class LeaderboardAdapter : ListAdapter<LeaderboardItem, LeaderboardAdapter.LeaderboardViewHolder>(LeaderboardItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.leaderboard_item, parent, false)

        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val item = getItem(position)
        holder.userName.text = item.name
        holder.userPoints.text = item.points.toString()
        holder.userImage.setImageResource(R.drawable.fui_ic_anonymous_white_24dp)
    }

    class LeaderboardViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val userPoints: TextView = itemView.findViewById(R.id.user_points)
        val userImage: ImageView = itemView.findViewById(R.id.user_image)
    }

    class LeaderboardItemCallback: DiffUtil.ItemCallback<LeaderboardItem>() {
        override fun areItemsTheSame(oldItem: LeaderboardItem, newItem: LeaderboardItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: LeaderboardItem,
            newItem: LeaderboardItem
        ): Boolean {
            return oldItem == newItem
        }

    }


}
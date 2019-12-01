package com.walkly.walkly.ui.leaderboard

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.walkly.walkly.R
import com.walkly.walkly.databinding.LeaderboardItemBinding

class LeaderboardAdapter :
    ListAdapter<LeaderboardItem, LeaderboardAdapter.LeaderboardViewHolder>(LeaderboardItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = LeaderboardItemBinding.inflate(layoutInflater, parent, false)

        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val item = getItem(position)
        holder.position.text = (position + 1).toString()
        holder.userName.text = item.name
        holder.userLevel.append(item.level.toString())
        holder.userPoints.text = item.points.toString()
        holder.userImage.setImageResource(R.drawable.fui_ic_anonymous_white_24dp)
    }

    class LeaderboardViewHolder(binding: LeaderboardItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val position: TextView = binding.userPosition
        val userName: TextView = binding.userName
        val userLevel: TextView = binding.userLevel
        val userPoints: TextView = binding.userPoints
        val userImage: ImageView = binding.userImage
    }

    class LeaderboardItemCallback : DiffUtil.ItemCallback<LeaderboardItem>() {
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
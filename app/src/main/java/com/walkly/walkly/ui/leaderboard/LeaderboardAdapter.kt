package com.walkly.walkly.ui.leaderboard

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.walkly.walkly.R
import com.walkly.walkly.databinding.LeaderboardItemBinding
import kotlinx.android.synthetic.main.leaderboard_item.view.*
import kotlinx.android.synthetic.main.list_leaderboard.view.*
import java.lang.Exception

class LeaderboardAdapter :
    ListAdapter<LeaderboardItem, LeaderboardAdapter.LeaderbardViewHolder2>(LeaderboardItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderbardViewHolder2 {

//        val layoutInflater = LayoutInflater.from(parent.context)
//        val binding = LeaderboardItemBinding.inflate(layoutInflater, parent, false)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_leaderboard, parent, false)
        return LeaderbardViewHolder2(view)
    }

    override fun onBindViewHolder(holder: LeaderbardViewHolder2, position: Int) {
        val item = getItem(position)
        /*holder.position.text = (position + 1).toString()
        holder.userName.text = item.name
        val level = "Level: ${item.level}"
        holder.userLevel.text = level
        holder.userPoints.text = item.points.toString()
        holder.userImage.setImageResource(R.drawable.fui_ic_anonymous_white_24dp)*/
        holder.bind(item)
    }

    class LeaderboardViewHolder(binding: LeaderboardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val position: TextView = binding.userPosition
        val userName: TextView = binding.userName
        val userLevel: TextView = binding.userLevel
        val userPoints: TextView = binding.userPoints
        val userImage: ImageView = binding.userImage
    }

    class LeaderbardViewHolder2(view: View) : RecyclerView.ViewHolder(view) {
        private val userName: TextView = view.tv_username
        private val level: TextView = view.tv_level
        private val points: TextView = view.tv_points
        private val avatar: ImageView = view.img_avatar
        private val rank: TextView = view.tv_rank

        fun bind(item: LeaderboardItem) {
            userName.text = item.name
            level.text = "level ${item.level}"
            points.text = "${item.points} points"
            rank.text = "${adapterPosition + 1}"

            try {
                Glide.with(this.itemView)
                    .load(item.photoURL)
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .into(avatar)
            } catch (e: Exception) {
                Log.d("leaderboard binding", "Failed to load avatar")
                avatar.setImageResource(R.drawable.ic_account_circle_black_24dp)
            }
        }
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
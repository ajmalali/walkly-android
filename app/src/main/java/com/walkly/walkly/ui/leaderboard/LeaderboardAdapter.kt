package com.walkly.walkly.ui.leaderboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.walkly.walkly.R

class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {
    var data = listOf<LeaderboardItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.leaderboard_item, parent, false)

        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val item = data[position]
        holder.userName.text = item.name
        holder.userPoints.text = item.points.toString()
        holder.userImage.setImageResource(R.drawable.fui_ic_anonymous_white_24dp)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class LeaderboardViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val userPoints: TextView = itemView.findViewById(R.id.user_points)
        val userImage: ImageView = itemView.findViewById(R.id.user_image)
    }


}
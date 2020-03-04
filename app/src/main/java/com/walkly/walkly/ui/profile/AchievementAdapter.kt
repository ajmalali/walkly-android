package com.walkly.walkly.ui.profile

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.walkly.walkly.R
import com.walkly.walkly.models.Achievement


class AchievementAdapter(var achievementList: List<Achievement>, private val listener: OnAchievementUseListener) : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.list_achievements, parent, false)

        return AchievementViewHolder(
            view,
            listener
        )
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        val item = achievementList[position]
        holder.achievementImage.setImageResource(R.drawable.achievement_placeholder)
        holder.achievementName.text = "${item.name} Achievement"
        holder.achievementEarned = item.earned
        val matrix = ColorMatrix()
        when (item.earned) {
            true -> matrix.setSaturation(1f)
            false -> {
                matrix.setSaturation(0f)
                holder.achievementName.setTextColor(Color.GRAY)
                holder.achievementLevel.setTextColor(Color.GRAY)
                holder.achievementPoints.setTextColor(Color.GRAY)
            }
        }
        val filter = ColorMatrixColorFilter(matrix)
        holder.achievementImage.colorFilter = filter
        holder.achievementLevel.text = "Level: ${item.level}"
        holder.achievementPoints.text = "${item.points} Points"
    }

    class AchievementViewHolder(itemView: View, private val listener: OnAchievementUseListener): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val achievementName: TextView = itemView.findViewById(R.id.achievement_name)
        val achievementLevel: TextView = itemView.findViewById(R.id.achievement_level)
        val achievementPoints: TextView = itemView.findViewById(R.id.achievement_points)
        val achievementImage: ImageView = itemView.findViewById(R.id.achievement_image)
        var achievementEarned: Boolean = false

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            listener.onAchievementClick(adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return achievementList.size
    }

    interface OnAchievementUseListener {
        fun onAchievementClick(position: Int)
    }
}


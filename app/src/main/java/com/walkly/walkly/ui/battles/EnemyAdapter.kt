package com.walkly.walkly.ui.battles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.walkly.walkly.R
import com.walkly.walkly.models.Enemy

class EnemyAdapter(
    var enemies: List<Enemy>,
    private val onEnemyListener: OnEnemyListener
) : RecyclerView.Adapter<EnemyAdapter.EnemyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnemyHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_host_battles, parent, false)
        return EnemyHolder(view, onEnemyListener)
    }

    override fun getItemCount(): Int {
        return enemies.size
    }

    override fun onBindViewHolder(holder: EnemyHolder, position: Int) {
        val enemy = enemies[position]
        holder.apply {
            this.enemy = enemy
            battleName.text = enemy.name
        }
    }

    inner class EnemyHolder(view: View, private val listener: OnEnemyListener) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        var enemy: Enemy? = null
        val battleName: TextView = itemView.findViewById(R.id.tv_battle_name_host)
        var background: androidx.constraintlayout.widget.ConstraintLayout =
            itemView.findViewById(R.id.host_bg)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            listener.onEnemyClick(adapterPosition)
        }
    }

    interface OnEnemyListener {
        fun onEnemyClick(position: Int)
    }

}
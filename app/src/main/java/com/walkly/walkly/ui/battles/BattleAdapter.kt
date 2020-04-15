package com.walkly.walkly.ui.battles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.walkly.walkly.R
import com.walkly.walkly.models.OnlineBattle

class BattleAdapter(
    var onlineBattles: List<OnlineBattle>,
    private val onBattleListener: OnBattleListener
) : RecyclerView.Adapter<BattleAdapter.BattleHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BattleHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_join_battles, parent, false)
        return BattleHolder(view, onBattleListener)
    }

    override fun getItemCount(): Int {
        return onlineBattles.size
    }

    override fun onBindViewHolder(holder: BattleHolder, position: Int) {
        val battle = onlineBattles[position]
        holder.apply {
            // Default image
            battleName.text = battle.battleName
            battleHost.text = battle.hostName
            val text = "${battle.playerCount}/4 Players"
            playerCount.text = text
            battleID = battle.id.toString()
        }
    }

    inner class BattleHolder(view: View, private val listener: OnBattleListener) :
        RecyclerView.ViewHolder(view),
        View.OnClickListener {
        val battleName: TextView = itemView.findViewById(R.id.tv_battle_name)
        val battleHost: TextView = itemView.findViewById(R.id.tv_battle_name_host)
        val playerCount: TextView = itemView.findViewById(R.id.tv_players)
        var background: androidx.constraintlayout.widget.ConstraintLayout =
            itemView.findViewById(R.id.join_bg)
        var battleID: String = ""

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            listener.onBattleClick(adapterPosition)
        }
    }

    interface OnBattleListener {
        fun onBattleClick(position: Int)
    }
}
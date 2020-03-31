package com.walkly.walkly.ui.battles

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.mapboxsdk.style.layers.Property
import com.walkly.walkly.R
import com.walkly.walkly.models.OnlineBattle
import com.walkly.walkly.onlineBattle.OnlineBattleActivity
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.ui.lobby.LobbyActivity
import kotlinx.android.synthetic.main.bottom_sheet_layout.*
import kotlinx.android.synthetic.main.fragment_host_join_battle.*

class BattlesFragment : Fragment() {

    private lateinit var battlesRecyclerView: RecyclerView

    private var battleAdapter: BattleAdapter? = null
    private var enemyAdapter: EnemyAdapter? = null

    private val battlesViewModel: BattlesViewModel by viewModels()
    private var isHosting: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_host_join_battle, container, false)
        battlesRecyclerView = view.findViewById(R.id.battles_recycler_view)

        // join button listener
        val joinBtn: RadioButton = view.findViewById(R.id.join_battle_button)
        joinBtn.setOnClickListener {
            hideHeader()
            pvp_host.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            battlesRecyclerView.adapter = null
            battlesViewModel.getOnlineBattles()
        }

        // host button listener
        val hostBtn: RadioButton = view.findViewById(R.id.host_button)
        hostBtn.setOnClickListener {
            pvp_host.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            battlesRecyclerView.adapter = null
            battlesViewModel.getEnemies()
        }

        // initialize recycler view
        battlesViewModel.onlineBattleList.observe(viewLifecycleOwner, Observer { list ->
            list?.let {
                if (joinBtn.isChecked) {
                    if (list.isEmpty()) {
                        progressBar.visibility = View.GONE
                        // Display no battles
                    } else {
                        battleAdapter = BattleAdapter(list)
                        battlesRecyclerView.adapter = battleAdapter
                        progressBar.visibility = View.GONE
                    }
                }
            }
        })

        battlesViewModel.enemyList.observe(viewLifecycleOwner, Observer { list ->
            list?.let {
                if (hostBtn.isChecked) {
                    if (list.isEmpty()) {
                        progressBar.visibility = View.GONE
                    } else {
                        enemyAdapter = EnemyAdapter(list)
                        battlesRecyclerView.adapter = enemyAdapter
                        progressBar.visibility = View.GONE
                    }
                }
            }
        })

        // TODO: FIX
        battlesViewModel.createBattle.observe(viewLifecycleOwner, Observer {
            battlesViewModel.currentPlayer.joinBattle()

            val intent = Intent(activity, LobbyActivity::class.java)

            intent.putExtra("battle", it)
            startActivity(intent)
            activity?.finish()
        })

        return view
    }

    private inner class BattleHolder(view: View) : RecyclerView.ViewHolder(view),
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
            //this.background.setBackgroundColor(Color.parseColor("#340055"))
            battlesViewModel.joinListener(this.battleID)

            battlesViewModel.currentPlayer.joinBattle()
            val intent = Intent(activity, OnlineBattleActivity::class.java)
            val bundle = Bundle()
            bundle.putString("battleId", battleID)
            intent.putExtras(bundle)
            startActivity(intent)
            activity?.finish()
        }
    }

    private inner class BattleAdapter(var onlineBattles: List<OnlineBattle>) :
        RecyclerView.Adapter<BattleHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BattleHolder {
            val view = layoutInflater.inflate(R.layout.list_join_battles, parent, false)
            return BattleHolder(view)
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
    }

    private inner class EnemyHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        var enemy: Enemy? = null
        val battleName: TextView = itemView.findViewById(R.id.tv_battle_name_host)
        var background: androidx.constraintlayout.widget.ConstraintLayout =
            itemView.findViewById(R.id.host_bg)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            // Add your on click logic here
            // this.background.setBackgroundColor(Color.parseColor("#340055"))
            showHeader()
            val enemyHealth = "HP: ${this.enemy?.health}"
            tv_enemy_health.text = enemyHealth
            val enemyName = this.enemy?.name
            tv_enemy_name.text = enemyName
            val level = "Level: ${this.enemy?.level}"
            tv_enemy_level.text = level

            create_button.setOnClickListener {
                isHosting = true
                battlesViewModel.selectEnemy(enemy!!)
            }

        }
    }

    private inner class EnemyAdapter(var enemies: List<Enemy>) :
        RecyclerView.Adapter<EnemyHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnemyHolder {
            val view = layoutInflater.inflate(R.layout.list_host_battles, parent, false)
            return EnemyHolder(view)
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
    }

    private fun hideHeader() {
        enemy_image.visibility = View.INVISIBLE
        create_button.visibility = View.INVISIBLE
        tv_enemy_health.visibility = View.INVISIBLE
        tv_enemy_level.visibility = View.INVISIBLE
        tv_enemy_name.visibility = View.INVISIBLE
    }

    private fun showHeader() {
        enemy_image.visibility = View.VISIBLE
        create_button.visibility = View.VISIBLE
        tv_enemy_health.visibility = View.VISIBLE
        tv_enemy_level.visibility = View.VISIBLE
        tv_enemy_name.visibility = View.VISIBLE
    }
}

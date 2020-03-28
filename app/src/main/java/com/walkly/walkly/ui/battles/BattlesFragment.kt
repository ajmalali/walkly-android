package com.walkly.walkly.ui.battles

import android.graphics.Color
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.walkly.walkly.R
import com.walkly.walkly.models.Battle
import com.walkly.walkly.models.OnlineBattle
import com.walkly.walkly.models.Player
import com.walkly.walkly.offlineBattle.OfflineBattle
import com.walkly.walkly.onlineBattle.OnlineBattleActivity
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.ui.lobby.LobbyActivity
import kotlinx.android.synthetic.main.fragment_battles.*
import kotlinx.android.synthetic.main.fragment_host_join_battle.*

class BattlesFragment : Fragment() {

    private lateinit var battlesRecyclerView: RecyclerView

    private var adapter: BattleAdapter? = null
    private var adapter2: EnemyAdapter? = null

    private val battlesViewModel: BattlesViewModel by lazy {
        ViewModelProviders.of(this).get(BattlesViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_host_join_battle, container, false)
        battlesRecyclerView = view.findViewById(R.id.battles_recycler_view)

        // initalize recylcer view
        battlesViewModel.battleList.observe(this, Observer { list ->
            list?.let {
                if(list.isEmpty()) {
                    progressBar.visibility = View.GONE
                } else {
                    adapter = BattleAdapter(list)
                    battlesRecyclerView.adapter = adapter
                    progressBar.visibility = View.GONE
                }
            }
        })

        // join button listner
        val joinBtn: RadioButton = view.findViewById(R.id.join_button)
       joinBtn.setOnClickListener {
           hideHeader()
           battlesViewModel.battleList.observe(this, Observer { list ->
               list?.let {
                   if(list.isEmpty()) {
                       progressBar.visibility = View.GONE
                   } else {
                       adapter = BattleAdapter(list)
                       battlesRecyclerView.adapter = adapter
                       progressBar.visibility = View.GONE
                   }
               }
           })
       }

        // host button lisetner
        val hostBtn: RadioButton = view.findViewById(R.id.host_button)
        hostBtn.setOnClickListener {
            battlesViewModel.enemyList.observe(this, Observer { list ->
                list?.let {
                    if(list.isEmpty()) {
                        progressBar.visibility = View.GONE
                    } else {
                        adapter2 = EnemyAdapter(list)
                        battlesRecyclerView.adapter = adapter2
                        progressBar.visibility = View.GONE
                    }
                }
            })
        }

        return view
    }

    private inner class BattleHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {
        val battleName: TextView = itemView.findViewById(R.id.tv_battle_name)
        val battleHost: TextView = itemView.findViewById(R.id.tv_battle_name_host)
        val playerCount: TextView = itemView.findViewById(R.id.tv_players)
        var background: androidx.constraintlayout.widget.ConstraintLayout = itemView.findViewById(R.id.join_bg)
        var battleID: String = ""

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            // Add your on click logic here
            //this.background.setBackgroundColor(Color.parseColor("#340055"))
            battlesViewModel.joinListner(this.battleID)

            Player.joinedBattle()
            val intent = Intent(activity, OnlineBattleActivity::class.java)
            val bundle = Bundle()
            bundle.putString("battleId", battleID)
            intent.putExtras(bundle)
            startActivity(intent)
            activity?.finish()
        }
    }

    private inner class BattleAdapter(var battles: List<Battle>): RecyclerView.Adapter<BattleHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BattleHolder {
            val view = layoutInflater.inflate(R.layout.list_join_battles, parent, false)
            return BattleHolder(view)
        }

        override fun getItemCount(): Int {
            return battles.size
        }

        override fun onBindViewHolder(holder: BattleHolder, position: Int) {
            val battle = battles[position]
            holder.apply {
                // Default image
                battleName.text = battle.battleName
                battleHost.text = battle.host
                playerCount.text = "${battle.playerCount}/4 Players"
                battleID = battle.id
            }
        }
    }

    private inner class EnemyHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {
        val battleName: TextView = itemView.findViewById(R.id.tv_battle_name_host)
        var enemyName =""
        var background: androidx.constraintlayout.widget.ConstraintLayout = itemView.findViewById(R.id.host_bg)
        var enemyLevel = 0
        var enemyHP = 0


        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            // Add your on click logic here
            // this.background.setBackgroundColor(Color.parseColor("#340055"))
            showHeader()
            tv_enemy_health.text = "HP: ${this.enemyHP.toString()}"
            tv_enemy_name.text = "${this.enemyName}"
            tv_enemy_level.text = "Level: ${this.enemyLevel.toString()}"

            create_button.setOnClickListener {
                battlesViewModel.hostListner(this.enemyName, this.enemyHP)
                battlesViewModel.hostedBattleID.observe(activity!!, Observer {battle_ID->
                    Player.joinedBattle()
                    val intent = Intent(activity, LobbyActivity::class.java)
                    val bundle = Bundle()
                    bundle.putString("battleId", battle_ID)
                    bundle.putString("enemyName", this.enemyName)
                    bundle.putString("enemyHP", this.enemyHP.toString())
                    bundle.putString("enemyLvl", this.enemyLevel.toString())

                    intent.putExtras(bundle)
                    startActivity(intent)
                    activity?.finish()
                })
            }
        }
    }


    private inner class EnemyAdapter(var enemies: List<Enemy>): RecyclerView.Adapter<EnemyHolder>() {
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
//                battleName.text = enemy.name.value.toString()
//                enemyName = enemy.name.value.toString()
//                enemyHP = enemy.HP.value!!.toInt()
//                enemyLevel = enemy.level.value!!.toInt()
            }
        }
    }

    private fun hideHeader(){
        enemy_image.visibility = View.INVISIBLE
        create_button.visibility = View.INVISIBLE
        tv_enemy_health.visibility = View.INVISIBLE
        tv_enemy_level.visibility = View.INVISIBLE
        tv_enemy_name.visibility = View.INVISIBLE
    }

    private fun showHeader(){
        enemy_image.visibility = View.VISIBLE
        create_button.visibility = View.VISIBLE
        tv_enemy_health.visibility = View.VISIBLE
        tv_enemy_level.visibility = View.VISIBLE
        tv_enemy_name.visibility = View.VISIBLE
    }
}

package com.walkly.walkly.ui.battles

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.walkly.walkly.R
import com.walkly.walkly.onlineBattle.OnlineBattleActivity
import com.walkly.walkly.pvp.PvPActivity
import com.walkly.walkly.ui.lobby.OnlineLobbyActivity
import com.walkly.walkly.ui.lobby.PvPLobbyActivity
import kotlinx.android.synthetic.main.fragment_host_join_battle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "BattlesFragment"

class BattlesFragment : Fragment(), BattleAdapter.OnBattleListener, EnemyAdapter.OnEnemyListener,
    InvitesAdapter.OnInviteListener {

    private lateinit var battlesRecyclerView: RecyclerView
    private lateinit var invitesRecyclerView: RecyclerView

    private lateinit var battleAdapter: BattleAdapter
    private lateinit var enemyAdapter: EnemyAdapter
    private lateinit var invitesAdapter: InvitesAdapter

    private lateinit var loadingDialog: AlertDialog
    private lateinit var loadingInflater: View

    private val battlesViewModel: BattlesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_host_join_battle, container, false)

        battlesRecyclerView = view.findViewById(R.id.battles_recycler_view)
        invitesRecyclerView = view.findViewById(R.id.invites_recycler_view)

        battleAdapter = BattleAdapter(mutableListOf(), this)
        enemyAdapter = EnemyAdapter(mutableListOf(), this)
        invitesAdapter = InvitesAdapter(mutableListOf(), this)

        // join button listener
        val joinBtn: RadioButton = view.findViewById(R.id.join_battle_button)
        joinBtn.setOnClickListener {
            hideHeader()
            tv_pvp_invites.visibility = View.VISIBLE
            tv_online_battles.visibility = View.VISIBLE
            invitesRecyclerView.visibility = View.VISIBLE

            pvp_host.visibility = View.GONE
            battlesRecyclerView.adapter = null
            battlesViewModel.getInvites()
            battlesViewModel.getOnlineBattles()
        }

        // host button listener
        val hostBtn: RadioButton = view.findViewById(R.id.host_button)
        hostBtn.setOnClickListener {
            tv_pvp_invites.visibility = View.INVISIBLE
            tv_online_battles.visibility = View.INVISIBLE
            invitesRecyclerView.visibility = View.GONE

            pvp_host.visibility = View.VISIBLE
            battlesRecyclerView.adapter = null
            battlesViewModel.getEnemies()
        }

        // Observe online battles (real time)
        battlesViewModel.onlineBattleList.observe(viewLifecycleOwner, Observer { list ->
            list?.let {
                if (joinBtn.isChecked) {
                    if (list.isEmpty()) {
                        // Display no battles
                    } else {
                        battleAdapter = BattleAdapter(list, this)
                        battlesRecyclerView.adapter = battleAdapter
                    }
                }
            }
        })

        // Observe enemy
        battlesViewModel.enemyList.observe(viewLifecycleOwner, Observer { list ->
            list?.let {
                if (hostBtn.isChecked) {
                    if (list.isEmpty()) {
                        // Display no enemies
                    } else {
                        enemyAdapter = EnemyAdapter(list, this)
                        battlesRecyclerView.adapter = enemyAdapter
                    }
                }
            }
        })

        // Observe battle invites (real time)
        battlesViewModel.invitesList.observe(viewLifecycleOwner, Observer { list ->
            list?.let {
                if (joinBtn.isChecked) {
                    if (list.isEmpty()) {
                        // Display no invites
                    } else {
                        invitesAdapter = InvitesAdapter(list, this)
                        invitesRecyclerView.adapter = invitesAdapter
                    }
                }
            }
        })

        battlesViewModel.pvpBattle.observe(viewLifecycleOwner, Observer {
            val intent = Intent(activity, PvPActivity::class.java)
            intent.putExtra("battle", it)
            startActivity(intent)
            activity?.finish()
        })

        battlesViewModel.getInvites()
        battlesViewModel.getOnlineBattles()
        battlesViewModel.getEnemies()

        val pvpHost = view.findViewById<ConstraintLayout>(R.id.pvp_host)
        pvpHost.setOnClickListener {
            val battle = battlesViewModel.sendPvPInvite()
            val intent = Intent(activity, PvPLobbyActivity::class.java)
            intent.putExtra("battle", battle)
            startActivity(intent)
            activity?.finish()
        }

        // Creating battle dialog
        loadingInflater = layoutInflater.inflate(R.layout.dialog_loading_battle, container, false)
        loadingDialog = AlertDialog.Builder(context)
            .setView(loadingInflater)
            .create()

        loadingDialog.setCancelable(false)
        loadingDialog.setCanceledOnTouchOutside(false)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "Removing listeners")
        battlesViewModel.removeListeners()
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

    // TODO: FIX
    override fun onBattleClick(position: Int) {
        //this.background.setBackgroundColor(Color.parseColor("#340055"))
        var battle = battleAdapter.onlineBattles[position]
        CoroutineScope(IO).launch {
            withContext(Main) {
                loadingInflater.findViewById<TextView>(R.id.loading_text).text =
                    getString(R.string.joining_battle)
                loadingDialog.show()
            }

            battle = battlesViewModel.joinBattle(battle)

            withContext(Main) {
                val intent: Intent
                if (battle.battleState == "In-lobby") {
                    intent = Intent(activity, OnlineLobbyActivity::class.java)
                } else {
                    intent = Intent(activity, OnlineBattleActivity::class.java)
                }

                intent.putExtra("battle", battle)
                loadingDialog.dismiss()
                startActivity(intent)
                activity?.finish()
            }
        }
    }

    override fun onEnemyClick(position: Int) {
//         this.background.setBackgroundColor(Color.parseColor("#340055"))
        showHeader()
        val enemy = enemyAdapter.enemies[position]
        val enemyHealth = "HP: ${enemy.health}"
        tv_enemy_health.text = enemyHealth
        val enemyName = enemy.name
        tv_enemy_name.text = enemyName
        val level = "Level: ${enemy.level}"
        tv_enemy_level.text = level

        Glide.with(this)
            .load(enemy.image)
            .into(enemy_image)

        create_button.setOnClickListener {
            CoroutineScope(IO).launch {
                withContext(Main) {
                    loadingInflater.findViewById<TextView>(R.id.loading_text).text =
                        getString(R.string.creating_online_battle)
                    loadingDialog.show()
                }

                val battle = battlesViewModel.createOnlineBattle(enemy)

                withContext(Main) {
                    val intent = Intent(activity, OnlineLobbyActivity::class.java)
                    intent.putExtra("battle", battle)
                    loadingDialog.dismiss()
                    startActivity(intent)
                    activity?.finish()
                }
            }
        }
    }

    override fun onInviteClick(position: Int) {
        val invite = invitesAdapter.invites[position]
//        this.background.setBackgroundColor(Color.parseColor("#340055"))
        battlesViewModel.joinPvPListener(invite.id)
    }
}

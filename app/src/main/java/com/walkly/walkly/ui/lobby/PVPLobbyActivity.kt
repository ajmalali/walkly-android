package com.walkly.walkly.ui.lobby

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestoreException
import com.walkly.walkly.R
import com.walkly.walkly.models.PVPBattle
import com.walkly.walkly.pvp.PvPActivity
import com.walkly.walkly.ui.profile.EquipmentAdapter
import com.walkly.walkly.ui.profile.WearEquipmentViewModel
import kotlinx.android.synthetic.main.activity_online_lobby.*
import kotlinx.android.synthetic.main.activty_pvp_lobby.*
import kotlinx.android.synthetic.main.activty_pvp_lobby.btn_change_equipment_lobby
import kotlinx.android.synthetic.main.activty_pvp_lobby.current_player_avatar
import kotlinx.android.synthetic.main.activty_pvp_lobby.current_player_equipment_lobby
import kotlinx.android.synthetic.main.activty_pvp_lobby.current_player_name
import kotlinx.android.synthetic.main.activty_pvp_lobby.loading_bar
import kotlinx.android.synthetic.main.activty_pvp_lobby.start_button
import kotlinx.android.synthetic.main.activty_pvp_lobby.tv_waiting_lobby
import kotlinx.android.synthetic.main.dialog_wear_equipment.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "PvPLobbyActivity"

class PVPLobbyActivity : AppCompatActivity(), EquipmentAdapter.OnEquipmentUseListener {

    private lateinit var wearEquipmentDialog: AlertDialog
    private lateinit var wearEquipmentBuilder: AlertDialog.Builder
    private lateinit var adapter: EquipmentAdapter

    private val viewModel: PVPLobbyViewModel by viewModels()
    private val equipmentViewModel: WearEquipmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activty_pvp_lobby)
        supportActionBar!!.hide()

        val battle = intent.getParcelableExtra<PVPBattle>("battle")
        battle?.let {
            if (battle.host?.id == viewModel.userID) {
                displayBattleControls()
            } else {
                displayWaiting()
            }
        }

        viewModel.battle.observe(this, Observer { updatedBattle ->
            Log.d(TAG, "Updated Battle: $updatedBattle")
            if (updatedBattle.opponent != null) {
                start_button.isEnabled = true
                start_button.background.alpha = 255
            }
            setupPlayers(updatedBattle)

            if (updatedBattle.status == "In-game") {
                val intent = Intent(this, PvPActivity::class.java)
                intent.putExtra("battle", updatedBattle)
                startActivity(intent)
                this.finish()
            }
        })

        start_button.isEnabled = false
        start_button.background.alpha = 100

        // Wear Equipment Dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_wear_equipment, null, false)
        wearEquipmentBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
        wearEquipmentDialog = wearEquipmentBuilder.create()
        //To make the background for the dialog Transparent
        wearEquipmentDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        adapter =
            EquipmentAdapter(mutableListOf(), this)
        val rv = dialogView.findViewById(R.id.equipment_recycler_view) as RecyclerView
        rv.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        rv.adapter = adapter

        dialogView.progressBar.visibility = View.VISIBLE
        equipmentViewModel.equipments.observe(this, Observer { list ->
            dialogView.progressBar.visibility = View.GONE
            adapter.equipmentList = list
            if (list.size < 5) {
                rv.layoutManager =
                    GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false)
            } else {
                rv.layoutManager =
                    GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
            }
            adapter.notifyDataSetChanged()
        })

        battle?.let {
            viewModel.setupBattleListener(battle.id)
            setupPlayers(battle)
        }

        start_button.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    viewModel.changeBattleState("In-game")
                    // TODO: Display "Game starting in 3.."
                } catch (e: FirebaseFirestoreException) {
                    Log.d(TAG, "Error occurred: ${e.message}")
                }
            }
        }

        btn_change_equipment_lobby.setOnClickListener {
            wearEquipmentDialog.show()
        }

    }

    private fun displayWaiting() {
        start_button.visibility = View.GONE
        loading_bar.visibility = View.VISIBLE
        tv_waiting_lobby.visibility = View.VISIBLE
    }

    private fun displayBattleControls() {
        start_button.visibility = View.VISIBLE
        loading_bar.visibility = View.GONE
        tv_waiting_lobby.visibility = View.GONE
    }

    private fun setupPlayers(battle: PVPBattle) {
        val host = battle.host
        val opponent = battle.opponent

        var title = "${host?.name} vs ?"
        tv_pvp_title.text = title
        opponent?.let {
            var title = "${host?.name} vs ${opponent.name}"
            tv_pvp_title.text = title
        }

        if (host?.id == viewModel.userID) {
            current_player_name.text = host?.name
            Glide.with(this)
                .load(host?.avatarURL)
                .into(current_player_avatar)

            Glide.with(this)
                .load(host?.equipmentURL)
                .into(current_player_equipment_lobby)

            opponent?.let {
                opponent_name.text = opponent.name
                Glide.with(this)
                    .load(opponent.avatarURL)
                    .into(opponent_avatar)

                Glide.with(this)
                    .load(opponent.equipmentURL)
                    .into(opponent_equipment_pvp_lobby)
            }
        } else {
            current_player_name.text = opponent?.name
            Glide.with(this)
                .load(opponent?.avatarURL)
                .into(current_player_avatar)

            Glide.with(this)
                .load(opponent?.equipmentURL)
                .into(current_player_equipment_lobby)

            opponent_name.text = host?.name
            Glide.with(this)
                .load(host?.avatarURL)
                .into(opponent_avatar)

            Glide.with(this)
                .load(host?.equipmentURL)
                .into(opponent_equipment_pvp_lobby)
        }
    }

    override fun onEquipmentClick(position: Int) {
        val equipment = adapter.equipmentList[position]
        equipmentViewModel.selectEquipment(equipment)
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.changeEquipment(equipment)
            withContext(Dispatchers.Main) { wearEquipmentDialog.dismiss() }
        }
    }
}

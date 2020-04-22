package com.walkly.walkly.ui.lobby

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestoreException
import com.walkly.walkly.R
import com.walkly.walkly.models.PVPBattle
import com.walkly.walkly.pvp.PVPActivity
import com.walkly.walkly.ui.profile.EquipmentAdapter
import com.walkly.walkly.ui.profile.FriendsViewModel
import com.walkly.walkly.ui.profile.InviteFriendsAdapter
import com.walkly.walkly.ui.profile.WearEquipmentViewModel
import kotlinx.android.synthetic.main.activty_pvp_lobby.*
import kotlinx.android.synthetic.main.activty_pvp_lobby.btn_change_equipment_lobby
import kotlinx.android.synthetic.main.activty_pvp_lobby.current_player_avatar
import kotlinx.android.synthetic.main.activty_pvp_lobby.current_player_equipment_lobby
import kotlinx.android.synthetic.main.activty_pvp_lobby.current_player_name
import kotlinx.android.synthetic.main.activty_pvp_lobby.loading_bar
import kotlinx.android.synthetic.main.activty_pvp_lobby.start_button
import kotlinx.android.synthetic.main.activty_pvp_lobby.tv_waiting_lobby
import kotlinx.android.synthetic.main.dialog_invite_friend.view.*
import kotlinx.android.synthetic.main.dialog_wear_equipment.view.*
import kotlinx.android.synthetic.main.dialog_wear_equipment.view.progressBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "PvPLobbyActivity"

class PVPLobbyActivity : AppCompatActivity(), EquipmentAdapter.OnEquipmentUseListener,
    InviteFriendsAdapter.OnFriendInviteListener {

    private lateinit var wearEquipmentDialog: AlertDialog
    private lateinit var wearEquipmentBuilder: AlertDialog.Builder
    private lateinit var adapter: EquipmentAdapter

    private lateinit var leaveDialog: AlertDialog
    private lateinit var hostLeaveDialog: AlertDialog
    private lateinit var cancelledDialog: AlertDialog

    private lateinit var inviteFriendsDialog: AlertDialog
    private lateinit var inviteFriendsBuilder: AlertDialog.Builder
    private lateinit var inviteFriendsAdapter: InviteFriendsAdapter

    private val viewModel: PVPLobbyViewModel by viewModels()
    private val equipmentViewModel: WearEquipmentViewModel by viewModels()
    private val inviteFriendsViewModel: FriendsViewModel by viewModels()
    private var isHost = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activty_pvp_lobby)
        supportActionBar!!.hide()

        val battle = intent.getParcelableExtra<PVPBattle>("battle")
        battle?.let {
            if (battle.host?.id == viewModel.userID) {
                isHost = true
                displayBattleControls()
            } else {
                displayWaiting()
            }
        }

        initDialogs()

        viewModel.battle.observe(this, Observer { updatedBattle ->
            Log.d(TAG, "Updated Battle: $updatedBattle")
            if (updatedBattle.opponent != null) {
                start_button.isEnabled = true
                start_button.background.alpha = 255
            } else {
                start_button.isEnabled = false
                start_button.background.alpha = 100
            }

            setupPlayers(updatedBattle)

            if (updatedBattle.status == "In-game") {
                val intent = Intent(this, PVPActivity::class.java)
                intent.putExtra("battle", updatedBattle)
                startActivity(intent)
                this.finish()
            } else if (updatedBattle.status == "Cancelled") {
                cancelledDialog.show()
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

        // Invites only for the host
        if (isHost) {
            // Invite Friends Dialog
            val inviteDialog = layoutInflater.inflate(R.layout.dialog_invite_friend, null, false)
            inviteFriendsBuilder = AlertDialog.Builder(this)
                .setView(inviteDialog)
            inviteFriendsDialog = inviteFriendsBuilder.create()
            //To make the background for the dialog Transparent
            inviteFriendsDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            inviteFriendsAdapter = InviteFriendsAdapter(mutableListOf(), this)
            val r = inviteDialog.findViewById(R.id.invite_friends_recycler_view) as RecyclerView
            r.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false)
            r.adapter = inviteFriendsAdapter

            inviteDialog.progressBar.visibility = View.VISIBLE
            inviteFriendsViewModel.friendsList.observe(this, Observer { list ->
                inviteDialog.progressBar.visibility = View.GONE
                if (list.isEmpty()) {
                    inviteDialog.error_message.visibility = View.VISIBLE
                } else {
                    inviteDialog.error_message.visibility = View.GONE
                    inviteFriendsAdapter.friends = list
                    inviteFriendsAdapter.notifyDataSetChanged()
                }
            })
        }

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

        cancel_button.setOnClickListener {
            hostLeaveDialog.show()
        }

        leave_button.setOnClickListener {
            leaveDialog.show()
        }

        invite_friends.setOnClickListener {
            inviteFriendsDialog.show()
        }

    }

    private fun displayWaiting() {
        start_button.visibility = View.GONE
        cancel_button.visibility = View.GONE
        invite_friends.visibility = View.GONE
        loading_bar.visibility = View.VISIBLE
        tv_waiting_lobby.visibility = View.VISIBLE
        leave_button.visibility = View.VISIBLE
    }

    private fun displayBattleControls() {
        start_button.visibility = View.VISIBLE
        cancel_button.visibility = View.VISIBLE
        invite_friends.visibility = View.VISIBLE
        leave_button.visibility = View.GONE
        loading_bar.visibility = View.GONE
        tv_waiting_lobby.visibility = View.GONE
    }

    private fun setupPlayers(battle: PVPBattle) {
        val host = battle.host
        val opponent = battle.opponent

        if (isHost) {
            current_player_name.text = host?.name
            Glide.with(this)
                .load(host?.avatarURL)
                .into(current_player_avatar)

            Glide.with(this)
                .load(host?.equipmentURL)
                .into(current_player_equipment_lobby)

            if (opponent != null) {
                opponent_name.text = opponent.name
                Glide.with(this)
                    .load(opponent.avatarURL)
                    .into(opponent_avatar)

                opponent_equipment_pvp_lobby.visibility = View.VISIBLE
                Glide.with(this)
                    .load(opponent.equipmentURL)
                    .into(opponent_equipment_pvp_lobby)
            } else {
                opponent_name.text = "Waiting..."
                opponent_avatar.setImageResource(R.drawable.ic_account_circle_black_24dp)
                opponent_equipment_pvp_lobby.visibility = View.GONE
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

    override fun onBackPressed() {
        if (isHost) {
            hostLeaveDialog.show()
        } else {
            leaveDialog.show()
        }
    }

    private fun initDialogs() {
        // Leave Dialog
        val leaveInflater = layoutInflater.inflate(R.layout.dialog_leave_lobby, null)
        leaveDialog = AlertDialog.Builder(this)
            .setView(leaveInflater)
            .create()
        leaveDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        leaveInflater.findViewById<Button>(R.id.btn_leave)
            .setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.removeOpponent()
                    withContext(Dispatchers.Main) {
                        leaveDialog.dismiss()
                        finish()
                    }
                }
            }

        leaveInflater.findViewById<Button>(R.id.btn_stay)
            .setOnClickListener {
                leaveDialog.dismiss()
            }


        // Host Cancel Dialog
        val cancelInflater = layoutInflater.inflate(R.layout.dialog_leave_lobby_host, null)
        hostLeaveDialog = AlertDialog.Builder(this)
            .setView(cancelInflater)
            .create()
        hostLeaveDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        cancelInflater.findViewById<Button>(R.id.btn_yes)
            .setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.removeListeners()
                    viewModel.changeBattleState("Cancelled")
                    withContext(Dispatchers.Main) {
                        hostLeaveDialog.dismiss()
                        finish()
                    }
                }
            }

        cancelInflater.findViewById<Button>(R.id.btn_no)
            .setOnClickListener {
                hostLeaveDialog.dismiss()
            }

        // Battle Cancelled Dialog
        val battleCancelledInflater = layoutInflater.inflate(R.layout.dialog_battle_cancelled, null)
        cancelledDialog = AlertDialog.Builder(this)
            .setView(battleCancelledInflater)
            .create()
        cancelledDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        cancelledDialog.setCancelable(false)
        cancelledDialog.setCanceledOnTouchOutside(false)

        battleCancelledInflater.findViewById<Button>(R.id.btn_leave)
            .setOnClickListener {
                cancelledDialog.dismiss()
                finish()
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

    override fun onFriendInviteClick(position: Int) {
        val friend = inviteFriendsAdapter.friends[position]
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.inviteFriend(friend.id)
//            withContext(Main) { inviteFriendsDialog.dismiss() }
        }
    }
}

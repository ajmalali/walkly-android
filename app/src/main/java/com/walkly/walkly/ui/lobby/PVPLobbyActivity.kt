package com.walkly.walkly.ui.lobby

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.walkly.walkly.R
import com.walkly.walkly.models.PVPBattle
import com.walkly.walkly.pvp.PvPActivity
import com.walkly.walkly.ui.profile.EquipmentAdapter
import com.walkly.walkly.ui.profile.WearEquipmentViewModel

private const val TAG = "PvPLobbyActivity"

class PVPLobbyActivity : AppCompatActivity() {

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
            viewModel.setupBattleListener(battle.id)

        }


    }
}

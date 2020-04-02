package com.walkly.walkly.ui.lobby

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.walkly.walkly.R
import com.walkly.walkly.models.PvP
import com.walkly.walkly.pvp.PvPActivity
import com.walkly.walkly.repositories.PlayerRepository
import com.walkly.walkly.ui.battles.BattlesFragment

private const val TAG = "PvPLobbyActivity"

class PvPLobbyActivity : AppCompatActivity() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userID = FirebaseAuth.getInstance().currentUser?.uid
    val currentPlayer = PlayerRepository.getPlayer()

    lateinit var battle: PvP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activty_pvp_lobby)

    battle = intent.getParcelableExtra("battle") as PvP

        initListeners()
    }

    private fun initListeners() {
        db.collection("pvp_battles").document(battle.id!!)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    if (!snapshot.getString("opponentName").isNullOrEmpty()) {
                        battle = snapshot.toObject<PvP>()!!
                        val intent = Intent(this, PvPActivity::class.java)
                        intent.putExtra("battle", battle)
                        startActivity(intent)
                        this.finish()
                    }
                    Log.d(TAG, "Current data: ${snapshot.data}")
                } else {
                    Log.d(TAG, "Current data: null")
                }
            }
    }
}

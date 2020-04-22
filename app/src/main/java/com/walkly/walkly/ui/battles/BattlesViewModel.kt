package com.walkly.walkly.ui.battles

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.walkly.walkly.models.*
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.coroutines.tasks.await

private const val TAG = "BattlesViewModel"

class BattlesViewModel : ViewModel() {

    private var _pvpBattle = MutableLiveData<PVPBattle>()
    val pvpBattle: LiveData<PVPBattle>
        get() = _pvpBattle

    private val _battleList = MutableLiveData<List<OnlineBattle>>()
    val onlineBattleList: LiveData<List<OnlineBattle>>
        get() = _battleList

    private val _enemyList = MutableLiveData<List<Enemy>>()
    val enemyList: LiveData<List<Enemy>>
        get() = _enemyList

    private val _invitesList = MutableLiveData<List<BattleInvite>>()
    val invitesList: LiveData<List<BattleInvite>>
        get() = _invitesList

    private var tempBattleList = mutableListOf<OnlineBattle>()
    private var tempEnemyList = mutableListOf<Enemy>()
    private var tempInviteList = mutableListOf<BattleInvite>()

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userID = FirebaseAuth.getInstance().currentUser?.uid
    val currentPlayer = PlayerRepository.getPlayer()
    private val currentBattlePlayer = BattlePlayer(
        id = currentPlayer.id!!,
        name = currentPlayer.name!!,
        avatarURL = currentPlayer.photoURL!!,
        equipmentURL = currentPlayer.currentEquipment?.image!!,
        level = currentPlayer.level!!
    )

    private val HP_MULTIPLAYER = 100

    private lateinit var invitesRegistration: ListenerRegistration
    private lateinit var battlesRegistration: ListenerRegistration

    fun getInvites() {
        if (_invitesList.value == null) {
            Log.d(TAG, "ID: ${currentPlayer.id!!}")
            invitesRegistration = db.collection("invites")
                .whereArrayContains("toIDs", currentPlayer.id!!)
                .addSnapshotListener { value, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (value != null) {
                        tempInviteList.clear()
                        for (document in value) {
                            val invite = document.toObject<BattleInvite>()
                            tempInviteList.add(invite)
                        }
                        Log.d(TAG, "Got em $tempInviteList")
                        _invitesList.value = tempInviteList
                    } else {
                        _invitesList.value = emptyList()
                    }
                }
        } else {
            _invitesList.value = tempInviteList
        }
    }

    fun getEnemies() {
        if (_enemyList.value == null) {
            db.collection("online_enemies") // TODO: change to online enemies
                .get()
                .addOnSuccessListener { result ->
                    for (doc in result) {
                        val enemy = doc.toObject<Enemy>().addId(doc.id)
                        tempEnemyList.add(enemy)
                    }
                    Log.d(TAG, "Got enemies: ${tempEnemyList.size}")
                    _enemyList.value = tempEnemyList
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting enemy documents.", exception)
                }
        } else {
            _enemyList.value = tempEnemyList
        }
    }

    // Listening to online-battles in real time
    fun getOnlineBattles() {
        if (_battleList.value == null) {
            battlesRegistration = db.collection("online_battles")
                .whereEqualTo("type", "public")
                .whereIn("battleState", listOf("In-lobby", "In-game"))
                .addSnapshotListener { value, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (value != null) {
                        tempBattleList.clear()
                        for (document in value) {
                            val battle = document.toObject<OnlineBattle>()
                            tempBattleList.add(battle)
                        }
                        _battleList.value = tempBattleList
                    } else {
                        _battleList.value = emptyList()
                    }
                }
        } else {
            _battleList.value = tempBattleList
        }
    }

    // TODO: Change to OnlineEnemy
    suspend fun createOnlineBattle(enemy: Enemy): OnlineBattle {
        val battlePlayers = mutableListOf<BattlePlayer>()
        battlePlayers.add(currentBattlePlayer)

        val battle = OnlineBattle(
            battleName = enemy.name,
            enemy = enemy,
            hostName = currentPlayer.name,
            enemyHealth = enemy.health,
            players = battlePlayers,
            combinedPlayersHealth = (currentPlayer.level?.times(HP_MULTIPLAYER))?.toInt()
        )

        currentPlayer.joinBattle()

        val battlesRef = db.collection("online_battles").document()
        battle.id = battlesRef.id
        battlesRef.set(battle).await()

        val numShards = 4

        // Create counter collection and add a document to it
        val counterRef = battlesRef.collection("enemy_damage_counter")
            .document("enemy_damage_doc")

        counterRef.set(EnemyDamageCounter(numShards)).await()

        db.runBatch { batch ->
            val inviteDocument = db.collection("invites").document(battle.id!!)
            batch.set(
                inviteDocument, BattleInvite(
                    battleID = battle.id!!,
                    hostName = currentPlayer.name!!,
                    type = "ob"
                )
            )
            for (i in 0 until numShards) {
                val shardDocument = counterRef.collection("shards")
                    .document(i.toString())
                batch.set(shardDocument, Shard(0))
            }
        }.await()

        return battle
    }

    suspend fun getBattle(battleID: String): OnlineBattle? {
        return db.collection("online_battles").document(battleID).get().await()
            .toObject<OnlineBattle>()
    }

    // Join the selected battle
    suspend fun joinBattle(battle: OnlineBattle): OnlineBattle {
        battle.playerCount = battle.playerCount?.inc()
        battle.players.add(currentBattlePlayer)
        // Calculate combined base health
        var health: Long = 0
        for (i in battle.players.indices) {
            health += battle.players[i].level * HP_MULTIPLAYER
        }

        val battleRef = db.collection("online_battles").document(battle.id!!)

        // Update the battle in db
        battleRef.set(battle, SetOptions.merge()).await()

        // Update combined players health
        db.runTransaction { transaction ->
            val snapshot = transaction.get(battleRef)

            var combinedPlayersHealth = snapshot.getLong("combinedPlayersHealth")!!
            combinedPlayersHealth += (currentPlayer.level!! * HP_MULTIPLAYER)
            if (combinedPlayersHealth > health) {
                combinedPlayersHealth = health
            }

            transaction.update(battleRef, "combinedPlayersHealth", combinedPlayersHealth)
            combinedPlayersHealth.toInt()
        }

        // Remove current Invite
        db.collection("invites").document(battle.id!!)
            .update("toIDs", FieldValue.arrayRemove(currentPlayer.id))

        // Add player shards
        val numShards = 4
        val counterRef = battleRef
            .collection("enemy_damage_counter")
            .document("enemy_damage_doc")

        counterRef.update("numShards", FieldValue.increment(numShards.toLong())).await()

        db.runBatch { batch ->
            val startIndex = (battle.players.size - 1) * 4
            for (i in startIndex until startIndex + numShards) {
                val shardDocument = counterRef.collection("shards")
                    .document(i.toString())
                batch.set(shardDocument, Shard(0))
            }
        }.await()

        return battle
    }

    suspend fun createPVPBattle(): PVPBattle {
        // Create battle
        val document = db.collection("pvp_battles").document()
        val battle = PVPBattle(
            id = document.id,
            host = currentBattlePlayer
        )

        document.set(battle).await()

        // Add a global invite for now
        db.collection("invites").add(
            BattleInvite(
                battleID = battle.id,
                hostName = currentPlayer.name!!,
                type = "pvp"
            )
        ).await()

        // Initialize shard documents for both players
        val numShards = 4
        val hostDamageRef = db.collection("pvp_battles").document(battle.id)
            .collection("host_damage_counter")
            .document("host_damage_doc")

        val opponentDamageRef = db.collection("pvp_battles").document(battle.id)
            .collection("opponent_damage_counter")
            .document("opponent_damage_doc")

        db.runBatch { batch ->
            val inviteDocument = db.collection("invites").document(battle.id)
            batch.set(
                inviteDocument, BattleInvite(
                    battleID = battle.id,
                    hostName = currentPlayer.name!!,
                    type = "pvp"
                )
            )
            for (i in 0 until numShards) {
                val hostShardDoc = hostDamageRef.collection("shards")
                    .document(i.toString())
                val opponentShardDoc = opponentDamageRef.collection("shards")
                    .document(i.toString())

                batch.set(hostShardDoc, Shard(0))
                batch.set(opponentShardDoc, Shard(0))
            }
        }.await()

        return battle
    }

    suspend fun joinPVPBattle(id: String): PVPBattle? {
        currentPlayer.joinBattle()

        val battle = db.collection("pvp_battles").document(id).get()
            .await().toObject<PVPBattle>()

        battle?.opponent = currentBattlePlayer

        db.collection("pvp_battles").document(id).set(battle!!).await()
        db.collection("invites").document(id).delete()

        return battle
    }

    fun removeListeners() {
        invitesRegistration.remove()
        battlesRegistration.remove()
    }

}
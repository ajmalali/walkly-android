package com.walkly.walkly.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.walkly.walkly.models.Consumable
import com.walkly.walkly.models.Equipment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "EquipmentRepository"

// Singleton repository object
object EquipmentRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userID: String = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private val userDocument = db.collection("users").document(userID)

    private val equipmentList = mutableListOf<Equipment>()

    // Get Equipments of the current user
    suspend fun getEquipments(): MutableList<Equipment> {
        if (equipmentList.isNotEmpty()) {
            return equipmentList
        }

        val equipments = userDocument.collection("equipments").get().await()
        for (equipment in equipments.documents) {
            equipmentList.add(equipment.toObject<Equipment>()!!)
        }

        return equipmentList
    }

    fun addEquipment(equipment: Equipment) {
        equipmentList.add(equipment)
    }

    // TODO: Store locally when no internet
    suspend fun syncEquipment() {
        for (equipment in equipmentList) {
            userDocument.collection("equipments")
                .document(equipment.id!!).set(equipment).await()
        }
    }

    fun getDefaultEquipment(): Equipment {
        return Equipment(
            name = "Sword X",
            type = "attack",
            value = 5,
            level = 1,
            image = "https://firebasestorage.googleapis.com/v0/b/walkly-81d94.appspot.com/o/equipments%2F386arrzpkvO1j8Q4etKx.png?alt=media&token=2e860246-d0cd-46f6-bb80-b70756d36887"
        ).addId("gYQRFbka5BcjzbaocN6N")
    }
}
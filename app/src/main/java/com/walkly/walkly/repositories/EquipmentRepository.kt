package com.walkly.walkly.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.walkly.walkly.models.Equipment

private const val TAG = "EquipmentRepository"

// Singleton repository object
object EquipmentRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userID: String = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private val userDocument = db.collection("users").document(userID)
    val equipmentList = mutableListOf<Equipment>()
    private val eqIdList = mutableListOf<Equipment>()

    // Get Equipments of the current user
    fun getEquipment(callback: (List<Equipment>) -> Unit) {
        eqIdList.clear()
        equipmentList.clear()
        db.collection("equipments")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val equipment = document.toObject(Equipment::class.java).addId(document.id)
                    eqIdList.add(equipment)
                }
                userDocument.collection("equipments")
                    .get()
                    .addOnSuccessListener { res ->
                        for (document in res) {
                            val it: MutableIterator<Equipment> = eqIdList.iterator()
                            while (it.hasNext()) {
                                val eq: Equipment = it.next()
                                if (eq.id == document.id) {
                                    equipmentList.add(eq)
                                    Log.d(TAG, "added ${eq.id}")
                                }
                            }
                            Log.d(TAG, "List After: $equipmentList")
                            callback(equipmentList)
                        }
                    }

            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    fun wearEquipment(Equipment: Equipment, callback: (Equipment) -> Unit) {
        userDocument.update("equipment", Equipment.id)
            .addOnSuccessListener {
                Log.d(TAG, "Success updating Equipment")
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error updating equipment: ", exception)
            }

    }
}
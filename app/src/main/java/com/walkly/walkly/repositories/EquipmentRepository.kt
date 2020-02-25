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
    val eqIdList = mutableListOf<String>()

    // Get Equipments of the current user
    fun getEquipment(callback: (List<Equipment>) -> Unit) {
        db.collection("equipments")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val equipment = document.toObject(Equipment::class.java).addId(document.id)
                    equipmentList.add(equipment)
                    Log.d(TAG, "Added $document")
                }

                userDocument.collection("equipments")
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            val it: MutableIterator<Equipment> = equipmentList.iterator()
                            while (it.hasNext()) {
                                val eq: Equipment = it.next()
                                if (eq.id != document.id) {
                                    it.remove()
                                }
                            }
                        }
                    }

                callback(equipmentList)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    // Remove the given Equipment from the current user
    fun removeEquipment(Equipment: Equipment, callback: (List<Equipment>) -> Unit) {
        userDocument.collection("Equipments")
            .document(Equipment.id)
            .delete()
            .addOnSuccessListener {
                equipmentList.remove(Equipment)
                callback(equipmentList)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error deleting documents: ", exception)
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


    // Adds 3 new Equipments to the current user. just for test purposes
//    fun initEquipment() {
//        var ref = userDocument.collection("Equipments").document()
//        var Equipment = Equipment("Equipment 1", 2, "image", "health", 40).addId(ref.id)
//        ref.set(Equipment)
//            .addOnSuccessListener {
//                equipmentList.add(Equipment)
//            }
//
//        ref = userDocument.collection("Equipments").document()
//        Equipment = Equipment("Equipment 2", 3, "image", "attack", 30).addId(ref.id)
//        ref.set(Equipment)
//            .addOnSuccessListener {
//                equipmentList.add(Equipment)
//            }
//
//        ref = userDocument.collection("Equipments").document()
//        Equipment = Equipment("Equipment 3", 3, "image", "attack", 40).addId(ref.id)
//        ref.set(Equipment)
//            .addOnSuccessListener {
//                equipmentList.add(Equipment)
//            }
//    }
}
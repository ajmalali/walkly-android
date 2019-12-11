package com.walkly.walkly.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.walkly.walkly.models.Consumable

private const val TAG = "ConsumableRepository"

// Singleton repository object
object ConsumablesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userID: String = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private val userDocument = db.collection("users").document(userID)
    val consumableList = mutableListOf<Consumable>()

    // Get consumables of the current user
    fun getConsumables(callback: (List<Consumable>) -> Unit) {
        userDocument.collection("consumables")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val consumable = document.toObject(Consumable::class.java).addId(document.id)
                        consumableList.add(consumable)
                        Log.d(TAG, "Added $document")
                    }

                    callback(consumableList)
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting documents: ", exception)
                }
    }

    // Remove the given consumable from the current user
    fun removeConsumable(consumable: Consumable, callback: (List<Consumable>) -> Unit) {
        userDocument.collection("consumables")
                .document(consumable.id)
                .delete()
                .addOnSuccessListener {
                    consumableList.remove(consumable)
                    callback(consumableList)
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error deleting documents: ", exception)
                }
    }


    // Adds 3 new consumables to the current user. just for test purposes
    fun initConsumable() {
        var ref = userDocument.collection("consumables").document()
        var consumable = Consumable("consumable2", 2, "image", "health", 40).addId(ref.id)
        ref.set(consumable)
                .addOnSuccessListener {
                    consumableList.add(consumable)
                }

        ref = userDocument.collection("consumables").document()
        consumable = Consumable("consumable2", 3, "image", "attack", 30).addId(ref.id)
        ref.set(consumable)
                .addOnSuccessListener {
                    consumableList.add(consumable)
                }

        ref = userDocument.collection("consumables").document()
        consumable = Consumable("consumable2", 3, "image", "attack", 30).addId(ref.id)
        ref.set(consumable)
                .addOnSuccessListener {
                    consumableList.add(consumable)
                }
    }
}
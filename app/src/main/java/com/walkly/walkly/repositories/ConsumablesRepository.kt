package com.walkly.walkly.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.walkly.walkly.models.Consumable

private const val TAG = "ConsumableRepository"

// Singleton repository object
object ConsumablesRepository {

    private val db = FirebaseFirestore.getInstance()
    private var consumableList = mutableListOf<Consumable>()

    // Get consumables of the current user
    fun getConsumables(callback: (List<Consumable>) -> Unit) {
        val userID: String = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val userDocument = db.collection("users").document(userID)

        userDocument.collection("consumables")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val consumable = document.toObject(Consumable::class.java)
                    consumableList.add(consumable)
                }

                callback(consumableList)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }
}
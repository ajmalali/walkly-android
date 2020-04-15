package com.walkly.walkly.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.walkly.walkly.models.Consumable
import kotlinx.coroutines.tasks.await

private const val TAG = "ConsumableRepository"

// Singleton repository object
object ConsumablesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userID: String = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private val userDocument = db.collection("users").document(userID)
    private val collection = userDocument.collection("consumables")

    private val consumableList = mutableListOf<Consumable>()
    private val removedList = mutableListOf<Consumable>()

    // Get consumables of the current user
    suspend fun getConsumables(): MutableList<Consumable> {
        if (consumableList.isNotEmpty()) {
            return consumableList
        }

        val consumables = collection.get().await()
        for (consumable in consumables.documents) {
            consumableList.add(consumable.toObject<Consumable>()!!)
        }

        // For testing only
        if (consumableList.isEmpty()) {
            initConsumableList()
        }

        return consumableList
    }

    // Remove the given consumable from the current user
    fun removeConsumable(consumable: Consumable): MutableList<Consumable> {
        consumableList.remove(consumable)
        removedList.add(consumable)
        return consumableList
    }

    // TODO: Store locally when no internet
    suspend fun syncConsumables() {
        for (consumable in removedList) {
            collection.document(consumable.id!!).delete().await()
        }
        for (consumable in consumableList) {
            collection.document(consumable.id!!).set(consumable).await()
        }
    }


    // TODO: Add default consumables
    // Adds 3 new consumables to the current user. just for test purposes
    private fun initConsumableList() {
        var ref = userDocument.collection("consumables").document()
        var consumable = Consumable("consumable 1", 2, "image", "health", 40).addId(ref.id)
        consumableList.add(consumable)

        ref = userDocument.collection("consumables").document()
        consumable = Consumable("consumable 2", 3, "image", "attack", 30).addId(ref.id)
        consumableList.add(consumable)

        ref = userDocument.collection("consumables").document()
        consumable = Consumable("consumable 3", 3, "image", "attack", 40).addId(ref.id)
        consumableList.add(consumable)
    }
}
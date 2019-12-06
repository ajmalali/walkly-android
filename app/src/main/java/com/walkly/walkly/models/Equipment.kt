package com.walkly.walkly.models

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class Equipment(
    _image: String = "equipment",
    _name : String = "",
    _level:Int = 0,
    _type: String = "",
    _value: Int = 0,
    _id: String = "NoEquipmentsAreEquipped"
){
    var image = _image
    var name = _name
    var level = _level
    var type = _type
    var value = _value
    var id = _id

    val userEquipments = mutableListOf<Equipment>()

    private var userRef: DocumentReference
    private var equipmentRef: CollectionReference
    private var user: Map<String, Any>? = null

    private val auth = FirebaseAuth.getInstance()
    private val settings = FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(true)
        .build()
    private val firestore = FirebaseFirestore.getInstance().also {
        // caches the data locally to work offline
        it.firestoreSettings = settings
    }

    init {
        val uid = auth.currentUser?.uid
        Log.d("uid", uid)
        userRef = firestore.collection("users").document(uid!!)
        equipmentRef = userRef.collection("equipments")
        equipmentRef.get()
            .addOnSuccessListener {
                Log.d("init data", it.toString())

                // try read users equipments from the user doc
                try {
                    for(doc in it) {
                        userEquipments.add(doc as Equipment)
                    }
                } catch (tce: kotlin.TypeCastException) {
                    // does not have equipments in the collection
                    // add new doc in the collection with the default no equipment values
                    // create new doc for stamina
                    equipmentRef.document(this.id).set(
                        hashMapOf(
                            "image" to this.image,
                            "level" to this.level,
                            "name" to this.name,
                            "type" to this.type,
                            "value" to this.value
                        ), SetOptions.merge()
                    )
                }

            }
            .addOnFailureListener {
                // the user doesn't have equipments collection
                Log.e("init","firestore read", it)
                userRef.collection("equipments").document(this.id).set(
                    hashMapOf(
                        "image" to this.image,
                        "level" to this.level,
                        "name" to this.name,
                        "type" to this.type,
                        "value" to this.value
                    ), SetOptions.merge()
                )
            }
    }
}

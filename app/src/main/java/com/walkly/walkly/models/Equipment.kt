package com.walkly.walkly.models

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class Equipment(id: String) {
    val name = MutableLiveData<String>()
    val type = MutableLiveData<String>()
    val image = MutableLiveData<String>()
    val level = MutableLiveData<Long>()
    val value = MutableLiveData<Long>()

//    private val userEquipments = mutableListOf<Equipment>()
//    private val listOfEquipments = mutableListOf<Equipment>()
//    private var userEquipment = this
//    private var userRef: DocumentReference
//    private var equipmentRef: CollectionReference

    private val settings = FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(true)
        .build()
    private val firestore = FirebaseFirestore.getInstance().also {
        // caches the data locally to work offline
        it.firestoreSettings = settings
    }

    init {
        val equipmentRef = firestore.collection("equipments").document(id)
        equipmentRef.get()
            .addOnSuccessListener {
                name.value = it.data?.get("name") as String
                type.value = it.data?.get("type") as String
                image.value = it.data?.get("image") as String
                level.value = it.data?.get("level") as Long
                value.value = it.data?.get("value") as Long
            }
    }


//    fun getUsedEquipment(): Equipment {
//        userRef.get()
//            .addOnSuccessListener {
//                var usedEquipmentID = it.get("equipment").toString()
//                equipmentRef.document(usedEquipmentID).get()
//                    .addOnSuccessListener { result ->
//                        try {
//                            userEquipment = result.toObject(Equipment::class.java)!!
//                        } catch (tce: kotlin.TypeCastException) {
//                            userEquipment = this
//                            updateEquipment(this)
//                        }
//                    }
//                    .addOnFailureListener { result ->
//                        userEquipment = this
//                        updateEquipment(this)
//                    }
//            }
//        return userEquipment
//    }
//
//    fun getUserEquipments(): MutableList<Equipment> {
//        userRef.collection("equipments").get()
//            .addOnSuccessListener {
//                try {
//                    for (doc in it) {
//                        equipmentRef.document(doc.id).get()
//                            .addOnSuccessListener { result ->
//                                if (!userEquipments.contains(result as Equipment)) {
//                                    userEquipments.add(result as Equipment)
//                                }
//                            }
//                    }
//                } catch (tce: kotlin.TypeCastException) {
//                    // the user doesn't have equipments collection in his doc
//                    addNewEquipmentInUserInventory(this)
//                }
//            }
//            .addOnFailureListener {
//                // the user doesn't have equipments collection
//                Log.e("init", "firestore read", it)
//                addNewEquipmentInUserInventory(this)
//            }
//        return userEquipments
//    }
//
//    fun addNewEquipmentInUserInventory(equip: Equipment): MutableList<Equipment> {
//        if (!isEquipmentInitiated(equip)) {
//            addNewEquipment(equip)
//        }
//        userRef.collection("equipments").document(equip.id)
//        userEquipments.add(equip)
//        return userEquipments
//    }
//
//    fun addNewEquipment(equip: Equipment): MutableList<Equipment> {
//        var dublicate = false
//        if (listOfEquipments.isEmpty()) {
//            getAllEquipments()
//        }
//        if (listOfEquipments.contains(equip)) {
//            dublicate = true
//        }
//        if (!dublicate) {
//            //set doc
//            equipmentRef.document(equip.id).set(
//                hashMapOf(
//                    "image" to equip.image,
//                    "level" to equip.level,
//                    "name" to equip.name,
//                    "type" to equip.type,
//                    "value" to equip.value
//                ), SetOptions.merge()
//            )
//                .addOnSuccessListener {
//                    Log.d("success new equip", listOfEquipments.toTypedArray().toString())
//                }
//                .addOnFailureListener {
//                    Log.d("already new equip", listOfEquipments.toTypedArray().toString())
//                }
//
//            //update list
//            listOfEquipments.add(equip)
//        }
//        return listOfEquipments
//    }
//
//    fun getAllEquipments(): MutableList<Equipment> {
//        equipmentRef.get()
//            .addOnSuccessListener {
//                try {
//                    for (doc in it) {
//                        listOfEquipments.add(doc.toObject(Equipment::class.java))
//                    }
//                    for (i in listOfEquipments)
//                        Log.d("Equipment CLASS", i.toString())
//                } catch (tce: kotlin.TypeCastException) {
//                    addNewEquipment(this)
//                }
//            }
//            .addOnFailureListener {
//                addNewEquipment(this)
//            }
//        return listOfEquipments
//    }
//
//    private fun updateEquipment(equip: Equipment) {
//        if (isEquipmentInitiated(equip)) {
//            Log.d("updateEquip", equip.id)
//            userRef.set(
//                hashMapOf(
//                    "equipment" to equip.id
//                ), SetOptions.merge()
//            )
//        } else {
//            addNewEquipment(equip)
//            addNewEquipmentInUserInventory(equip)
//
//        }
//    }
//
//    private fun isEquipmentInitiated(equip: Equipment): Boolean {
//        return getAllEquipments().contains(equip)
//    }
//
}


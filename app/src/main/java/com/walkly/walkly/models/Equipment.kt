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

    private val userEquipments = mutableListOf<Equipment>()
    val listOfEquipments = mutableListOf<Equipment>()
    private var userEquipment:Equipment = this
        set(equip){
            this.userEquipment = equip
        }

    private var userRef: DocumentReference
    private var equipmentRef: CollectionReference

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
        equipmentRef = firestore.collection("equipments")
        //get all the equipment the user have
        getUserEquipments()
        //get used equipment
        getUsedEquipment()
        //get All Equipments
        getAllEquipments()
    }

    fun getUserEquipments(){
        userRef.collection("equipments").get()
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
                    addNewEquipmentInUserInventory(this)
                }
            }
            .addOnFailureListener {
                // the user doesn't have equipments collection
                Log.e("init","firestore read", it)
                addNewEquipmentInUserInventory(this)
            }
    }

    fun getAllEquipments() : MutableList<Equipment>{
        equipmentRef.get()
            .addOnSuccessListener {
                for(doc in it){
                    listOfEquipments.add(doc.toObject(Equipment::class.java))
                    Log.d("Equipment CLASS", listOfEquipments.toTypedArray().toString())
                }
            }
            .addOnFailureListener {
                addNewEquipment(this)
                listOfEquipments.add(equipmentRef.document(this.id).get().result!!.toObject(Equipment::class.java)!!)
            }
        return listOfEquipments
    }

    fun addNewEquipmentInUserInventory(equip: Equipment){
        userRef.collection("equipments").document(equip.id).set(
            hashMapOf(
                "image" to equip.image,
                "level" to equip.level,
                "name" to equip.name,
                "type" to equip.type,
                "value" to equip.value
            ), SetOptions.merge()
        )
    }

    fun addNewEquipment(equip:Equipment) : MutableList<Equipment>{
        equipmentRef.document(equip.id).set(hashMapOf(
            "image" to equip.image,
            "level" to equip.level,
            "name" to equip.name,
            "type" to equip.type,
            "value" to equip.value
        ), SetOptions.merge())
        listOfEquipments.add(equipmentRef.document(equip.id).get().result!!.toObject(Equipment::class.java)!!)
        return listOfEquipments
    }

    fun getUsedEquipment():Equipment{
        userRef.get()
            .addOnSuccessListener {
                var usedEquipmentRef = it.get("equipment").toString()
                equipmentRef.document(usedEquipmentRef).get()
                    .addOnSuccessListener {result ->
                        userEquipment = result.toObject(Equipment::class.java)!!
                    }
                    .addOnFailureListener{result ->
                        userEquipment = this

                    }
            }
        return userEquipment
    }

}

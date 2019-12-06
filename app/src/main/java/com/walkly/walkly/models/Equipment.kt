package com.walkly.walkly.models

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

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

    private var user: Map<String, Any>? = null
    private lateinit var userRef: DocumentReference
    private val auth = FirebaseAuth.getInstance()
    private val settings = FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(true)
        .build()
    private val firestore = FirebaseFirestore.getInstance().also {
        // caches the data locally to work offline
        it.firestoreSettings = settings
    }

    init {}
}

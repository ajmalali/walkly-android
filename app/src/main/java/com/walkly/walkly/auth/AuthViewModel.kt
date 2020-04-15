package com.walkly.walkly.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.walkly.walkly.models.Equipment
import com.walkly.walkly.models.Player
import com.walkly.walkly.repositories.EquipmentRepository
import kotlinx.coroutines.tasks.await

private const val TAG = "AuthViewModel"

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var deviceToken: String

    init {
        FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener {
                deviceToken = it.token
                Log.i("device token", it.token)
            }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun signIn(email: String, password: String): FirebaseUser? {
        // [START sign_in_with_email]
        val user: FirebaseUser?
        val result = auth.signInWithEmailAndPassword(email, password).await()
        user = result.user
        Log.d(TAG, "signInWithEmail:success")

        // TODO: this should not be in production code
        // it was used just to allow notification for old accounts
        val ref = db.collection("users").document(user?.uid!!)
        ref.set(
            Player(
                deviceToken = deviceToken,
                name = user.displayName,
                email = user.email,
                currentEquipment = Equipment.getDefaultEquipment(),
                photoURL = user.photoUrl.toString()
            ), SetOptions.merge()
        ).await()

        return user
        // [END sign_in_with_email]
    }

    // Creates an account for the user, sets profile picture, and calls initializePlayerInDB
    suspend fun createAccount(email: String, password: String, name: String): FirebaseUser? {
        var user: FirebaseUser? = null
        // [START create_user_with_email]
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        user = result.user
        Log.d(TAG, "createUserWithEmail:success: user: $user")

        val ref = storage.getReference("avatars/avatar_default.png")
        val uri = ref.downloadUrl.await()
        Log.d(TAG, "uri is $uri")

        val update = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .setPhotoUri(uri)
            .build()

        user?.updateProfile(update)?.await()
        Log.d(TAG, "Username was updated")
        initializePlayerInDB(user)

        return user
    }


    // initialize document for the newly created user. to avoid the ad-hoc try-catch mess
    private suspend fun initializePlayerInDB(user: FirebaseUser?) {
        // creating document in the users collections using newly created user's id as
        //  id of the document
        // TODO: discuss and add more fields

        val ref = db.collection("users").document(user?.uid!!)
        val defaultEquipment = EquipmentRepository.getDefaultEquipment()
        ref.set(
            Player(
                deviceToken = deviceToken,
                name = user.displayName,
                email = user.email,
                currentEquipment = defaultEquipment,
                photoURL = user.photoUrl.toString()
            )).await()

        // Add equipment to sub-collection
        ref.collection("equipments")
            .document(defaultEquipment.id!!)
            .set(defaultEquipment).await()

        Log.d(TAG, "new user document was initialized successfully \nuid=${user.uid}")
    }

}
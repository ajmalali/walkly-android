package com.walkly.walkly.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.walkly.walkly.models.Equipment
import kotlinx.coroutines.tasks.await

private const val TAG = "AuthViewModel"

//// TODO: Refactor
//private const val DEFAULT_WEAPON = "386arrzpkvO1j8Q4etKx"

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun signIn(email: String, password: String): FirebaseUser? {
        // [START sign_in_with_email]
        val user: FirebaseUser?
        val result = auth.signInWithEmailAndPassword(email, password).await()
        user = result.user
        Log.d(TAG, "signInWithEmail:success")

        return user
        // [END sign_in_with_email]
    }

    // Creates an account for the user, sets profile picture, and calls initializePlayer
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

        db.collection("users").document(user?.uid!!).set(
            hashMapOf(
                "name" to user.displayName,
                "email" to user.email,
                "stamina" to 300L,
                "points" to 0L,
                "level" to 1L,
                "progress" to 0L,
                // TODO: Change to default equipment
                "currentEquipment" to null,
                "currentHP" to 100,
                "phoneNumber" to user.phoneNumber,
                "lastUpdate" to null,
                "photoURL" to user.photoUrl.toString()
            ), SetOptions.merge()).await()

        Log.d(TAG, "new user document was initialized successfully \nuid=${user.uid}")
    }

}
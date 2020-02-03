package com.walkly.walkly.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.walkly.walkly.MainActivity
import com.walkly.walkly.R
import kotlinx.android.synthetic.main.activity_signup.*

class SignUpActivity : AppCompatActivity(), View.OnClickListener  {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        setContentView(R.layout.activity_signup)
        emailCreateAccountButton.setOnClickListener(this)
        auth = FirebaseAuth.getInstance()


    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    // [END on_start_check_user]

    private fun createAccount(email: String, password: String) {
        Log.d(DEBUG_TAG, "createAccount:$email")
        if (!validateForm()) {
            return
        }

        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(DEBUG_TAG, "createUserWithEmail:success")

                    // set profile name
                    val update = UserProfileChangeRequest.Builder()
                        .setDisplayName(fieldName.text.toString())
                        .setPhotoUri(null)
                        .build()

                    val user = auth.currentUser

                    user?.updateProfile(update)
                        ?.addOnSuccessListener {
                            Log.i(DEBUG_TAG, "user name was updated")
                        }

                    initializePlayer(user?.uid)


                    updateUI(user)

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(DEBUG_TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }

            }
        // [END create_user_with_email]
    }

    private fun validateForm(): Boolean {
        var valid = true

        val name = fieldName.text.toString()
        if (TextUtils.isEmpty(name)) {
            fieldName.error = "Required."
            valid = false
        } else {
            fieldName.error = null
        }

        val email = fieldEmail.text.toString()
        if (TextUtils.isEmpty(email)) {
            fieldEmail.error = "Required."
            valid = false
        } else {
            fieldEmail.error = null
        }

        val password = fieldPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            fieldPassword.error = "Required."
            valid = false
        } else {
            fieldPassword.error = null
        }

        return valid
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.emailCreateAccountButton -> createAccount(fieldEmail.text.toString(), fieldPassword.text.toString())
        }
    }
    companion object {
        const val DEBUG_TAG = "SignUpActivity"

        // id of the default equipment in the database.
        // it's hard coded form know
        const val DEFAULT_WEAPON = "386arrzpkvO1j8Q4etKx"
    }

    // initialize document for the newly created user. to avoid the ad-hoc try-catch mess
    private fun initializePlayer(uid: String?) {

        if (uid != null) {
            // creating document in the users collections using newly created user's id as
            //  id of the document
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(uid)

            userRef.set(
                hashMapOf(
                    "stamina" to 300L,
                    "points" to 0L,
                    "level" to 1L,
                    "progress" to 0L,
                    "equipment" to arrayListOf<String>(),
                    "equipped_weapon" to DEFAULT_WEAPON,
                    "friends" to arrayListOf<String>(),
                    // for the time being items are just arraylist referring to consumable item documents
                    // player cannot have multiple items of the same type
                    "items" to arrayListOf<String>()
                ), SetOptions.merge()
            ).addOnSuccessListener {
                Log.d(DEBUG_TAG, "new user document was initialized successfully \n" +
                        "uid=$uid")
            }

        }
    }
}

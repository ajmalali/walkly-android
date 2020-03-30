package com.walkly.walkly.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.walkly.walkly.MainActivity
import com.walkly.walkly.R
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "LOGIN"

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private val viewModel: AuthViewModel by viewModels()
    private val scope = CoroutineScope(IO)

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        // Buttons
        emailSignInButton.setOnClickListener(this)
        signUp.setOnClickListener(this)
    }

    // [START on_start_check_user]
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = viewModel.getCurrentUser()
        if (currentUser != null) {
            // TODO: Show loading
            scope.launch {
                PlayerRepository.initPlayer()
                withContext(Main) {
                    updateUI(currentUser)
                }
            }
        }
    }
    // [END on_start_check_user]


    private fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn:$email")

        if (!validateForm()) {
            return
        }

        // TODO: Add loading icon
        scope.launch {
            try {
                val user = viewModel.signIn(email, password)
                PlayerRepository.initPlayer()
                withContext(Main) {
                    updateUI(user)
                }
            } catch (e: FirebaseAuthException) {
                // If sign in fails, display a message to the user.
                displayMessage(e.message)
            }
            // [END create_user_with_email]
        }
    }

    // TODO: (UI) Change to snackbar
    private suspend fun displayMessage(message: String?) {
        // UI events happen only on main thread
        withContext(Main) {
            // If sign in fails, display a message to the user.
            Log.w(TAG, "Error occurred")
            Toast.makeText(
                baseContext, message ?: "No error message",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun validateForm(): Boolean {
        var valid = true

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

    private fun signUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.emailSignInButton -> signIn(
                fieldEmail.text.toString(),
                fieldPassword.text.toString()
            )
            R.id.signUp -> signUp()
        }
    }

}
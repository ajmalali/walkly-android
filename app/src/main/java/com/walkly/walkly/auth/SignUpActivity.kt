package com.walkly.walkly.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import com.walkly.walkly.MainActivity
import com.walkly.walkly.R
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.activity_signup.fieldEmail
import kotlinx.android.synthetic.main.activity_signup.fieldPassword
import kotlinx.android.synthetic.main.activity_signup.loading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DEBUG_TAG = "SignUpActivity"

class SignUpActivity : AppCompatActivity(), View.OnClickListener {

    private val viewModel: AuthViewModel by viewModels()
    private val scope = CoroutineScope(IO)

    public override fun onCreate(savedInstanceState: Bundle?) {
        // To remove status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_signup)
        emailCreateAccountButton.setOnClickListener(this)

        signIn.setOnClickListener {
            finish()
        }
    }

//    public override fun onStart() {
//        super.onStart()
//        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = viewModel.getCurrentUser()
//        loading.visibility = View.GONE
//        if (currentUser != null) {
//            // TODO: Show loading
//            scope.launch {
//                PlayerRepository.initPlayer()
//                withContext(Main) {
//                    updateUI(currentUser)
//                }
//            }
//        }
//    }

    private fun createAccount(email: String, password: String) {
        Log.d(DEBUG_TAG, "createAccount:$email")

        if (!validateForm()) {
            loading.visibility = View.GONE
            return
        }

        scope.launch {
            try {
                val name = fieldName.text.toString()
                val user = viewModel.createAccount(email, password, name)
                PlayerRepository.initPlayer()
                withContext(Main) {
                    loading.visibility = View.GONE
                    updateUI(user)
                }

            } catch (e: FirebaseAuthException) {
                withContext(Main) { loading.visibility = View.GONE }
                // If sign in fails, display a message to the user.
                displayMessage(e.message)
            } catch (e: FirebaseFirestoreException) {
                withContext(Main) { loading.visibility = View.GONE }
                displayMessage(e.message)
            } catch (e: Exception) {
                withContext(Main) { loading.visibility = View.GONE }
                Log.d(DEBUG_TAG, "${e.message}")
            }

            // [END create_user_with_email]
        }
    }

    // TODO: (UI) Change to snackbar
    private suspend fun displayMessage(message: String?) {
        withContext(Main) {
            // If sign in fails, display a message to the user.
            Log.w(DEBUG_TAG, "Error occurred")
            Toast.makeText(
                baseContext, message ?: "No error message",
                Toast.LENGTH_SHORT
            ).show()
        }
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
        loading.visibility = View.VISIBLE
        when (v.id) {
            R.id.emailCreateAccountButton -> {
                createAccount(fieldEmail.text.toString(), fieldPassword.text.toString())
            }
        }
    }
}

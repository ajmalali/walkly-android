package com.walkly.walkly

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.walkly.walkly.auth.AuthViewModel
import com.walkly.walkly.auth.LoginActivity
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity:AppCompatActivity() {
    private val viewModel: AuthViewModel by viewModels()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val splashTime = 3000L
    private lateinit var handler :Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        handler = Handler()

        handler.postDelayed({
            goToMainActivity()
        },
            splashTime
        )

    }

    private fun goToMainActivity() {
        val currentUser = viewModel.getCurrentUser()
        if (currentUser != null) {
            scope.launch {
                PlayerRepository.initPlayer()
                withContext(Main) {
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }else {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
//        val mainActivityIntent = Intent(applicationContext, LoginActivity::class.java)
//        startActivity(mainActivityIntent)
//        finish()
    }
}
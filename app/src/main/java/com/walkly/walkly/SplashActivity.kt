package com.walkly.walkly

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.walkly.walkly.auth.AuthViewModel
import com.walkly.walkly.auth.LoginActivity
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class SplashActivity : AppCompatActivity() {
    private val viewModel: AuthViewModel by viewModels()
    private val scope = CoroutineScope(Main)

    private val splashTime = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        // To remove status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_splash)

        val currentUser = viewModel.getCurrentUser()

        scope.launch {
            delay(splashTime)
            if (currentUser != null) {
                withContext(IO) { PlayerRepository.initPlayer() }
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onPause() {
        scope.cancel()
        super.onPause()
    }
}
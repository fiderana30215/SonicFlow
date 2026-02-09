package com.sonicflow.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.sonicflow.MainActivity
import com.sonicflow.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    
    companion object {
        private const val SPLASH_DURATION = 2000L // 2 seconds
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Get views
        val logoImageView = findViewById<ImageView>(R.id.splash_logo)
        val appNameTextView = findViewById<TextView>(R.id.splash_app_name)
        val taglineTextView = findViewById<TextView>(R.id.splash_tagline)
        
        // Load and start animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        
        logoImageView.startAnimation(fadeIn)
        appNameTextView.startAnimation(fadeIn)
        taglineTextView.startAnimation(fadeIn)
        
        // Navigate to MainActivity after delay
        lifecycleScope.launch {
            delay(SPLASH_DURATION)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }
    }
}

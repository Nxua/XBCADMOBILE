package com.jetdesk

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.jetdesk.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load the combined animation
        val combinedAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_animation)

        // Start animation for all elements
        binding.logo.startAnimation(combinedAnimation)
        binding.title.startAnimation(combinedAnimation)
        binding.slogan.startAnimation(combinedAnimation)

        // Delay to navigate to LoginActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3000) // 3 seconds delay
    }
}

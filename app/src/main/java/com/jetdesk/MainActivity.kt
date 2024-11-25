package com.jetdesk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jetdesk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example content for main activity
        binding.welcomeText.text = "Welcome to JetDesk!"
    }
}

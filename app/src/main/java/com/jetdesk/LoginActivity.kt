package com.jetdesk

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.jetdesk.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Inflate layout using ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toggle password visibility
        binding.passwordInput.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEndIndex = 2 // Index for end drawable
                if (event.rawX >= (binding.passwordInput.right - binding.passwordInput.compoundDrawables[drawableEndIndex].bounds.width())) {
                    togglePasswordVisibility(binding.passwordInput)
                    return@setOnTouchListener true
                }
            }
            false
        }

        // Handle login button click
        binding.loginButton.setOnClickListener {
            val email = binding.usernameInput.text.toString().trim()
            val password = binding.passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }

        // Handle forgot password click
        binding.forgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun togglePasswordVisibility(passwordField: EditText) {
        if (isPasswordVisible) {
            passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordField.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_lock, 0, R.drawable.view, 0
            )
        } else {
            passwordField.inputType = InputType.TYPE_CLASS_TEXT
            passwordField.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_lock, 0, R.drawable.closeeye, 0
            )
        }
        isPasswordVisible = !isPasswordVisible
        passwordField.setSelection(passwordField.text.length)
    }
}

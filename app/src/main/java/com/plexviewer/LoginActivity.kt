package com.plexviewer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.plexviewer.api.PlexApiManager
import com.plexviewer.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val TAG = "LoginActivity"
    private lateinit var plexApiManager: PlexApiManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        plexApiManager = PlexApiManager(this)

        val etUsername: EditText = binding.etUsername
        val etPassword: EditText = binding.etPassword
        val btnLogin: Button = binding.btnLogin

        Handler(Looper.getMainLooper()).postDelayed({
            etUsername.requestFocus()
        }, 200)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                plexApiManager.login(username, password, onSuccess = { authToken ->
                    getServers()
                }, onFailure = { errorMessage ->
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                })
            } else {
                Toast.makeText(
                    this,
                    "Du musst deinen Usernamen und das Passwort eingeben",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun changeActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getServers() {
        plexApiManager.getServers(onSuccess = { devices ->
            devices.forEach { device ->
                device.connections?.forEach { connection ->
                    Log.d(TAG, "Server: ${device.name}, Address: ${connection.address}, Port: ${connection.port}")
                }
            }
        }, onFailure = { errorMessage ->
            Log.e(TAG, errorMessage)
        })
    }
}

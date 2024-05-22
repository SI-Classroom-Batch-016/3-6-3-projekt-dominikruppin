package com.plexviewer

import PlexServer
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.plexviewer.adapter.ServerAdapter
import com.plexviewer.api.PlexApiManager
import com.plexviewer.databinding.ActivityLoginBinding
import com.plexviewer.databinding.ServerListDialogBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
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
                plexApiManager.login(username, password, onSuccess = { _, _, _ ->
                    Toast.makeText(this, "Login erfolgreich.", Toast.LENGTH_SHORT).show()
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

        plexApiManager.servers.observe(this) { servers ->
            if (servers != null) {
                showServerListDialog(servers)
            }
        }
    }

    private fun showServerListDialog(servers: List<PlexServer>) {
        val binding = ServerListDialogBinding.inflate(layoutInflater)
        val recyclerView = binding.serverListRecyclerView
        val parent = recyclerView.parent as ViewGroup
        parent.removeView(recyclerView)
        recyclerView.adapter = ServerAdapter(this, servers)

        val serverListDialog = AlertDialog.Builder(this)
            .setTitle("Wählen Sie einen Plex-Server")
            .setView(recyclerView)
            .create()

        serverListDialog.show()
    }
}

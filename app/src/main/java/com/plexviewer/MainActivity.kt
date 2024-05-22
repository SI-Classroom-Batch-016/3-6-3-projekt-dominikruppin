package com.plexviewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.plexviewer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = getSharedPreferences("Plex", Context.MODE_PRIVATE)
        val plexToken = sharedPreferences.getString("plex_token", null)
        val serverProtocol = sharedPreferences.getString("server_protocol", null)
        val serverAdress = sharedPreferences.getString("server_address", null)
        val serverPort = sharedPreferences.getString("server_port", null)
        Log.d("Server", "Token: $plexToken\nProtokoll: $serverProtocol\nAdresse: $serverAdress\nPort: $serverPort")
        super.onCreate(savedInstanceState)
        // Prüfen ob der PlexToken und Serverauswal existiert
        if (plexToken == null || serverProtocol == null || serverAdress == null || serverPort == null) {
            // Wenn irgendwas fehlt, starte Loginprozess
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()  // MainActivity schließen, damit der User nicht zurück kann
            return
        }
        // Binding inflaten
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Content anzeigen
        setContentView(binding.root)
        // Actionbar initialisieren
        setSupportActionBar(binding.appBarMain.toolbar)



        // Floating Action Button
        binding.appBarMain.fab.setOnClickListener { view ->
            // Fragment für die Settings einbinden
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Hamburgermenü Punkte hinzufügen
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_movie, R.id.nav_tvshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    // Dreipunkte Menü inflaten
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    // Dreipunkte Menü, Clicklistener
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val sharedPreferences = getSharedPreferences("Plex", Context.MODE_PRIVATE)
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_logout -> {
                val editor = sharedPreferences.edit()
                editor.remove("plex_token")
                editor.remove("server_protocol")
                editor.remove("server_address")
                editor.remove("server_port")
                editor.apply()
                Toast.makeText(this, "Erfolgreich ausgeloggt.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Zum navigieren und öffnen des Hamburgermenüs
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
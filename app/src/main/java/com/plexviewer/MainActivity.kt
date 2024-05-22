package com.plexviewer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        // Ruft die SharedPreferences (gespeicherten Werte) für Plex ab
        sharedPreferences = getSharedPreferences("Plex", Context.MODE_PRIVATE)
        // Lädt den gespeicherten Plextoken
        val plexToken = sharedPreferences.getString("plex_token", null)
        // Lädt das gespeicherte Serverprotokoll (http/https)
        val serverProtocol = sharedPreferences.getString("server_protocol", null)
        // Lädt die gespeicherte Adresse (URL/IP)
        val serverAdress = sharedPreferences.getString("server_address", null)
        // Lädt den gespeicherten Port
        val serverPort = sharedPreferences.getString("server_port", null)
        Log.d("Server", "Token: $plexToken\nProtokoll: $serverProtocol\nAdresse: $serverAdress\nPort: $serverPort")
        super.onCreate(savedInstanceState)
        // Prüfen ob der PlexToken und Serverauswal existiert
        if (plexToken == null || serverProtocol == null || serverAdress == null || serverPort == null) {
            // Wenn irgendwas fehlt, starte Loginprozess
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            // MainActivity schließen, damit der User nicht zurück kann
            finish()
            return
        }
        // Binding inflaten
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Content anzeigen
        setContentView(binding.root)
        // Toolbar als Actionbar initialisieren
        setSupportActionBar(binding.appBarMain.toolbar)

        // Floating Action Button
        binding.appBarMain.fab.setOnClickListener { view ->
            // Sortierung einbauen
        }

        // Hamburgermenü inflaten
        val drawerLayout: DrawerLayout = binding.drawerLayout
        // Navigation binden
        val navView: NavigationView = binding.navView
        // navController initialisieren
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Hamburgermenü Punkte hinzufügen
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_movie, R.id.nav_tvshow
            ), drawerLayout
        )
        // Richtet die ActionBar mit dem NavController ein
        setupActionBarWithNavController(navController, appBarConfiguration)
        // Richtet die NavigationView mit dem NavController ein
        navView.setupWithNavController(navController)
    }

    // Dreipunkte Menü inflaten
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    // Dreipunkte Menü, Clicklistener
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Einstellungen ausgewählt
            R.id.action_settings -> {
                // Aktuell nur ein Debughinweis
                Toast.makeText(this, "Einstellungen ausgewählt", Toast.LENGTH_SHORT).show()
                true
            }
            // Ausloggen ausgewählt
            R.id.action_logout -> {
                // Den Editor für die sharedPreferences laden
                val editor = sharedPreferences.edit()
                // Plextoken löschen
                editor.remove("plex_token")
                // Serverprotokoll (http/https) löschen
                editor.remove("server_protocol")
                // Server Addresse
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
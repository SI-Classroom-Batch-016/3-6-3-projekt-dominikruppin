package com.plexviewer.api

import Directory
import LibraryResponse
import Location
import PlexServer
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import retrofit2.http.GET
import retrofit2.http.Header
import java.net.HttpURLConnection
import java.net.URL

class PlexApiManager private constructor(context: Context) : ViewModel() {
    private val TAG = "PlexApiManager"
    private val sharedPreferences = context.getSharedPreferences("Plex", Context.MODE_PRIVATE)
    // Lädt den gespeicherten Plextoken
    val plexToken = sharedPreferences.getString("plex_token", null)
    // Lädt das gespeicherte Serverprotokoll (http/https)
    val serverProtocol = sharedPreferences.getString("server_protocol", null)
    // Lädt die gespeicherte Adresse (URL/IP)
    val serverAdress = sharedPreferences.getString("server_address", null)
    // Lädt den gespeicherten Port
    val serverPort = sharedPreferences.getString("server_port", null)
    // Lädt den usernamen
    val userName = sharedPreferences.getString("username", null)
    // Lädt den Avatar
    val avatar = sharedPreferences.getString("thumb", null)
    // Lädt die Movie Keys
    val movie = sharedPreferences.getString("movie", null)
    // Lädt die Serien Keys
    val shows = sharedPreferences.getString("shows", null)
    // Editor für sharedPreferences laden
    val editor = sharedPreferences.edit()

    // Liste der Server
    private val _servers = MutableLiveData<List<PlexServer>?>()
    val servers: LiveData<List<PlexServer>?>
        get() = _servers

    // Liste der Bibliotheken
    private val _libraries = MutableLiveData<List<Directory>>()
    val libraries: LiveData<List<Directory>>
        get() = _libraries

    private val _movies = MutableLiveData<List<Movie>>()

    val movies: LiveData<List<Movie>>
        get() = _movies

    // Retrofit Instanz zum Zugriff auf plex.tv (userobject)
    private val fixedRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://plex.tv/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val fixedService: PlexApi = fixedRetrofit.create(PlexApi::class.java)

    // Funktion zum holen des Userobjects für Token, Avatar und Username
    fun login(
        username: String,
        password: String,
        onSuccess: (String, String, String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val productId = "Plex Viewer for Android"
        val product = "Plex Viewer for Android"
        val version = "1.0.0"

        val call = fixedService.login(
            productId,
            product,
            version,
            username,
            password
        )
        call.enqueue(object : Callback<PlexUserResponse> {
            override fun onResponse(
                call: Call<PlexUserResponse>,
                response: Response<PlexUserResponse>
            ) {
                Log.d(TAG, "Antwort vom Server: ${response.raw()}")
                if (response.isSuccessful) {
                    response.body()?.let {
                        val authToken = it.user.authToken
                        Log.d(TAG, "Token: $authToken")
                        val userName = it.user.username
                        Log.d(TAG, "Username: $userName")
                        val userThumb = it.user.thumb
                        Log.d(TAG, "Thumb: $userThumb")
                        saveData(authToken, userName, userThumb)
                        onSuccess(authToken, userName, userThumb)
                    } ?: run {
                        Log.e(TAG, "Leere Antwort erhalten")
                        onFailure("Login fehlgeschlagen")
                    }
                } else {
                    Log.e(TAG, "Login fehlgeschlagen: ${response.errorBody()?.string()}")
                    onFailure("Login fehlgeschlagen")
                }
            }

            override fun onFailure(call: Call<PlexUserResponse>, t: Throwable) {
                Log.e(TAG, "Netzwerkfehler: ${t.message}", t)
                onFailure("Netzwerkfehler: ${t.message}")
            }
        })
    }

    fun getMovies() {
        val movieKey = sharedPreferences.getString("movie", null)

        if (plexToken == null) {
            Log.e(TAG, "Kein Token vorhanden")
            return
        }

        if (movieKey == null) {
            Log.e(TAG, "Kein Movie Key in den SharedPreferences gespeichert")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Filme werden abgerufen...")
            val url =
                URL("$serverProtocol://$serverAdress:$serverPort/library/sections/$movieKey/all?X-Plex-Token=$plexToken")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Antwort wurde empfangen")
                val inputStream = connection.inputStream
                val xmlParser = XmlPullParserFactory.newInstance().newPullParser()
                xmlParser.setInput(inputStream, null)

                var eventType = xmlParser.eventType
                val movies = mutableListOf<Movie>()

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && xmlParser.name == "Video") {
                        val title = xmlParser.getAttributeValue(null, "title")
                        val year = xmlParser.getAttributeValue(null, "year")?.toIntOrNull()
                        val thumb = xmlParser.getAttributeValue(null, "thumb")

                        if (title != null && year != null && thumb != null) {
                            // Bilde die vollständige URL für das Coverbild
                            val coverImageUrl = "$serverProtocol://$serverAdress:$serverPort$thumb?X-Plex-Token=$plexToken"
                            movies.add(Movie(title, year, coverImageUrl))
                        }

                        // Stop parsing after collecting 100 movies
                        if (movies.size >= 100) {
                            break
                        }
                    }
                    eventType = xmlParser.next()
                }

                Log.d(TAG, "Filme: $movies")
                _movies.postValue(movies)
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Filme: ${responseCode}")
            }
        }
    }



    // Funktion zum abrufen der Bibliotheken
    fun getLibraries() {
        if (plexToken == null) {
            Log.e(TAG, "Kein Token vorhanden")
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Bibliotheken werden abgerufen...")
            val url =
                URL("$serverProtocol://$serverAdress:$serverPort/library/sections?X-Plex-Token=$plexToken")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Antwort wurde empfangen")
                val inputStream = connection.inputStream
                val xmlParser = XmlPullParserFactory.newInstance().newPullParser()
                xmlParser.setInput(inputStream, null)

                var eventType = xmlParser.eventType
                val directories = mutableListOf<Directory>()

                var movieKey: String? = null
                var showKey: String? = null

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && xmlParser.name == "Directory") {
                        val allowSync = xmlParser.getAttributeValue(null, "allowSync").toInt()
                        val art = xmlParser.getAttributeValue(null, "art")
                        val filters = xmlParser.getAttributeValue(null, "filters").toInt()
                        val refreshing = xmlParser.getAttributeValue(null, "refreshing").toInt()
                        val thumb = xmlParser.getAttributeValue(null, "thumb")
                        val key = xmlParser.getAttributeValue(null, "key")
                        val type = xmlParser.getAttributeValue(null, "type")
                        val title = xmlParser.getAttributeValue(null, "title")
                        val agent = xmlParser.getAttributeValue(null, "agent")
                        val scanner = xmlParser.getAttributeValue(null, "scanner")
                        val language = xmlParser.getAttributeValue(null, "language")
                        val uuid = xmlParser.getAttributeValue(null, "uuid")
                        val updatedAt = xmlParser.getAttributeValue(null, "updatedAt").toLong()
                        val createdAt = xmlParser.getAttributeValue(null, "createdAt").toLong()
                        val locations = mutableListOf<Location>()

                        while (eventType != XmlPullParser.END_TAG || xmlParser.name != "Directory") {
                            if (eventType == XmlPullParser.START_TAG && xmlParser.name == "Location") {
                                val id = xmlParser.getAttributeValue(null, "id").toInt()
                                val path = xmlParser.getAttributeValue(null, "path")
                                locations.add(Location(id, path))
                            }
                            eventType = xmlParser.next()
                        }

                        directories.add(
                            Directory(key, type, title)
                        )
                        // Prüfen ob die Bibliothek vom Typ Filme ist und noch kein Key gesetzt ist
                        if (type == "movie" && movieKey == null) {
                            Log.d(TAG, "Movie gefunden. Key: $key")
                            // Den Key der Bibliothek als movieKey setzen
                            movieKey = key
                        // Prüfen ob die Bibliothek vom Typ Serien ist und noch kein Key gesetzt ist
                        } else if (type == "show" && showKey == null) {
                            Log.d(TAG, "Serie gefunden. Key: $key")
                            // Den Key der Bibliothek als serienKey setzen
                            showKey = key
                        }
                    }
                    eventType = xmlParser.next()
                }
                // Editor aufrufen
                val editor = sharedPreferences.edit()
                // den String in movieKey in den SharedPreferences unter dem Key movie speichern
                movieKey?.let {
                    editor.putString("movie", it)
                }
                // den String in showKey in den SharedPreferences unter dem Key show speichern
                showKey?.let {
                    editor.putString("show", it)
                }
                // Änderungen speichern
                editor.apply()

                Log.d(TAG, "Bibliotheken: $directories")
                // Bibliotheken in die MutableLiveData pushen
                _libraries.postValue(directories)
            } else {
                Log.e(TAG, "Fehler beim Abrufen der Bibliotheken: ${responseCode}")
            }
        }
    }


    fun logout() {
        editor.remove("plex_token")
        // Serverprotokoll (http/https) löschen
        editor.remove("server_protocol")
        // Server Addresse löschen
        editor.remove("server_address")
        // Server Port löschen
        editor.remove("server_port")
        // Änderungen speichern
        editor.apply()
    }

    // Dient zum speichern in den SharedPreferences
    private fun saveData(token: String, username: String, userThumb: String) {
        Log.d(TAG, "Username: $username")
        Log.d(TAG, "Thumblink: $userThumb")
        Log.d(TAG, "Token wird gespeichert: $token")
        editor.putString("plex_token", token)
        editor.putString("thumb", userThumb)
        editor.putString("username", username)
        editor.apply()
        fetchAvailableServers(token)
    }

    // Dient dem asynchronen abrufen der Server (Coroutine wird genutzt)
    private fun fetchAvailableServers(authToken: String) {
        CoroutineScope(Dispatchers.Main).launch {
            _servers.value = withContext(Dispatchers.IO) {
                getAvailableServers(authToken)
            }
            Log.d(TAG, "Serverliste: ${_servers.value}")
        }
    }

    // Wir erstellen eine einzelne globale Instanz für alle Activitys
    companion object {
        @Volatile
        private var INSTANCE: PlexApiManager? = null

        fun getInstance(context: Context): PlexApiManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PlexApiManager(context)
                INSTANCE = instance
                instance
            }
        }
    }
}

// Funktion zum abrufen der Server
fun getAvailableServers(authToken: String): List<PlexServer> {
    val TAG = "API"
    Log.d(TAG, "Server werden abgerufen...")
    val url = URL("https://plex.tv/pms/resources?X-Plex-Token=$authToken")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"

    val responseCode = connection.responseCode
    if (responseCode == HttpURLConnection.HTTP_OK) {
        Log.d(TAG, "Antwort wurde empfangen")
        val inputStream = connection.inputStream
        val xmlParser = XmlPullParserFactory.newInstance().newPullParser()
        xmlParser.setInput(inputStream, null)

        var eventType = xmlParser.eventType
        val serverList = mutableListOf<PlexServer>()

        Log.d(TAG, "Prüfe die XML Datei")
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && xmlParser.name == "Device") {
                val product = xmlParser.getAttributeValue(null, "product")
                if (product == "Plex Media Server") {
                    val deviceName = xmlParser.getAttributeValue(null, "name")
                    val connections = mutableListOf<Pair<String, Pair<String, Int>>>()

                    var connectionTag = xmlParser.next()
                    while (connectionTag != XmlPullParser.END_TAG || xmlParser.name != "Device") {
                        if (connectionTag == XmlPullParser.START_TAG && xmlParser.name == "Connection") {
                            val protocol = xmlParser.getAttributeValue(null, "protocol")
                            val address = xmlParser.getAttributeValue(null, "address")
                            val port = xmlParser.getAttributeValue(null, "port").toInt()
                            connections.add(Pair(protocol, Pair(address, port)))
                        }
                        connectionTag = xmlParser.next()
                    }

                    connections.forEach { (protocol, addressPort) ->
                        serverList.add(
                            PlexServer(
                                deviceName,
                                protocol,
                                addressPort.first,
                                addressPort.second
                            )
                        )
                    }
                }
            }
            eventType = xmlParser.next()
        }
        Log.d(TAG, "Serverliste: $serverList")
        return serverList
    } else {
        Log.d(TAG, "Fehlgeschlagen...")
        return emptyList()
    }
}

// API Endpunkte und Header
interface PlexApi {
    @FormUrlEncoded
    @POST("users/sign_in.json")
    fun login(
        @Field("X-Plex-Client-Identifier") clientId: String,
        @Field("X-Plex-Product") product: String,
        @Field("X-Plex-Version") version: String,
        @Field("user[login]") username: String,
        @Field("user[password]") password: String
    ): Call<PlexUserResponse>

    @GET("library/sections")
    fun getLibraries(@Header("X-Plex-Token") token: String): Call<LibraryResponse>
}

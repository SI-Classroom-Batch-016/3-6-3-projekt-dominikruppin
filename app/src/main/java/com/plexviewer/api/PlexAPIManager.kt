package com.plexviewer.api

import PlexServer
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
import java.net.HttpURLConnection
import java.net.URL

class PlexApiManager(private val context: Context) {

    private val TAG = "PlexApiManager"
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://plex.tv/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service: PlexApi = retrofit.create(PlexApi::class.java)

    fun login(
        username: String,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val productId = "Plex Viewer for Android"
        val product = "Plex Viewer for Android"
        val version = "1.0.0"

        val call = service.login(
            productId,
            product,
            version,
            username,
            password
        )
        call.enqueue(object : Callback<PlexToken> {
            override fun onResponse(call: Call<PlexToken>, response: Response<PlexToken>) {
                Log.d(TAG, "Antwort vom Server: ${response.raw()}")
                if (response.isSuccessful) {
                    response.body()?.let {
                        val authToken = it.user.authToken
                        saveToken(authToken)
                        onSuccess(authToken)
                    } ?: run {
                        Log.e(TAG, "Leere Antwort erhalten")
                        onFailure("Login fehlgeschlagen")
                    }
                } else {
                    Log.e(TAG, "Login fehlgeschlagen: ${response.errorBody()?.string()}")
                    onFailure("Login fehlgeschlagen")
                }
            }

            override fun onFailure(call: Call<PlexToken>, t: Throwable) {
                Log.e(TAG, "Netzwerkfehler: ${t.message}", t)
                onFailure("Netzwerkfehler: ${t.message}")
            }
        })
    }

    private fun saveToken(token: String) {
        Log.d(TAG, "Token wird gespeichert: $token")
        val sharedPreferences = context.getSharedPreferences("Plex", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("plex_token", token)
        editor.apply()
        fetchAvailableServers(token)
    }

    private fun fetchAvailableServers(authToken: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val serverList = withContext(Dispatchers.IO) {
                getAvailableServers(authToken)
            }
            Log.d("API", "Serverliste: $serverList")
        }
    }
}

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
                        serverList.add(PlexServer(deviceName, protocol, addressPort.first, addressPort.second))
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

interface PlexApi {
    @FormUrlEncoded
    @POST("users/sign_in.json")
    fun login(
        @Field("X-Plex-Client-Identifier") clientId: String,
        @Field("X-Plex-Product") product: String,
        @Field("X-Plex-Version") version: String,
        @Field("user[login]") username: String,
        @Field("user[password]") password: String
    ): Call<PlexToken>
}



package com.plexviewer.api

import android.content.Context
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

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
    }

    fun getServers(onSuccess: (List<Device>) -> Unit, onFailure: (String) -> Unit) {
        val sharedPreferences = context.getSharedPreferences("Plex", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("plex_token", null)

        if (token == null) {
            onFailure("Token not found")
            return
        }

        Log.d(TAG, "Server werden abgerufen...")
        val call = service.getServers(token)
        call.enqueue(object : Callback<ServerList> {
            override fun onResponse(call: Call<ServerList>, response: Response<ServerList>) {
                Log.d(TAG, "Antwort vom Server: ${response.raw()}")
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it.devices ?: listOf())
                    } ?: run {
                        Log.e(TAG, "Response body is null")
                        onFailure("Failed to retrieve servers")
                    }
                } else {
                    Log.e(TAG, "Failed to retrieve servers: ${response.errorBody()?.string()}")
                    onFailure("Failed to retrieve servers")
                }
            }

            override fun onFailure(call: Call<ServerList>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}", t)
                onFailure("Network error: ${t.message}")
            }
        })
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

    @GET("api/resources?includeHttps=1")
    fun getServers(@Header("X-Plex-Token") token: String): Call<ServerList>
}



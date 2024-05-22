package com.plexviewer.api

import com.google.gson.annotations.SerializedName

data class PlexUserResponse(
    @SerializedName("user")
    val user: User,
    @SerializedName("authentication_token")
    val authToken: String
)

data class User(
    @SerializedName("id")
    val id: Int,
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("joined_at")
    val joinedAt: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("thumb")
    val thumb: String,
    @SerializedName("hasPassword")
    val hasPassword: Boolean,
    @SerializedName("authToken")
    val authToken: String
)
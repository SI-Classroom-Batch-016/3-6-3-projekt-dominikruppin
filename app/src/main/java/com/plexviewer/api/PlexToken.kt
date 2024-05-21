package com.plexviewer.api

data class PlexToken(
    val user: User
) {
    data class User(
        val authToken: String
    )
}
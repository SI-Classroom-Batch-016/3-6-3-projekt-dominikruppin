package com.plexviewer.api

// Datenklasse für die Filme
data class Movie(
    val title: String,
    val year: Int,
    val coverImageUrl: String
)
package com.plexviewer.api

import com.google.gson.annotations.SerializedName

data class LibraryResponse(
    @SerializedName("MediaContainer")
    val mediaContainer: MediaContainer
)

data class MediaContainer(
    @SerializedName("Directory")
    val directories: List<Directory>
)

data class Directory(
    @SerializedName("key")
    val key: String,
    @SerializedName("title")
    val title: String
)
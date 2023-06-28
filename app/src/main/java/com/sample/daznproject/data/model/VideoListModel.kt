package com.sample.daznproject.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class VideoListModel(
    @Expose
    @SerializedName("name")
    val name: String?,

    @Expose
    @SerializedName("uri")
    val videoUri: String?
)
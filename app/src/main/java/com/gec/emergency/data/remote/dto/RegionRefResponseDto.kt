package com.gec.emergency.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegionRefResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("active") val active: Boolean = true,
)

package com.example.bogoargo.data.dto.response

import com.google.gson.annotations.SerializedName

data class SelfieResultResponseDto(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("result")
    val result: SelfieResultDto
)

data class SelfieResultDto(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("result")
    val result: String
)
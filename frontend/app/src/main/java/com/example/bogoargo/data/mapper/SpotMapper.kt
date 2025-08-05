package com.example.bogoargo.data.mapper

import com.example.bogoargo.data.dto.response.SpotDataDto

// SpotDataDto를 위한 변환 함수들
fun SpotDataDto.getSpotId(): Int = this.spotId
fun SpotDataDto.getSpotName(): String = this.spotName
fun SpotDataDto.getLatitude(): Double = this.latitude
fun SpotDataDto.getLongitude(): Double = this.longitude
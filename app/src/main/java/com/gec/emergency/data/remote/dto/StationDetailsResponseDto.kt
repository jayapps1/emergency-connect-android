package com.gec.emergency.data.remote.dto

data class StationDetailsResponseDto(
    val station: StationResponseDto,
    val contacts: List<StationContactResponseDto> = emptyList(),
)

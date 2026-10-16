package com.gec.emergency.data.mapper

import com.gec.emergency.data.remote.dto.StationResponseDto
import com.gec.emergency.domain.model.Station

fun StationResponseDto.toDomainModel(): Station {
    return Station(
        id = id,
        agencyId = agencyId,
        name = name,
        code = code,
        stationLevel = stationLevel,
        region = region,
        district = district,
        regionId = regionId,
        districtId = districtId,
        locality = locality,
        latitude = latitude,
        longitude = longitude,
        locationAccuracyMeters = locationAccuracyMeters,
        isActive = active,
        isVerified = verified,
    )
}

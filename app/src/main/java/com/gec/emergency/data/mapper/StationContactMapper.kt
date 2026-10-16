package com.gec.emergency.data.mapper

import com.gec.emergency.data.remote.dto.StationContactResponseDto
import com.gec.emergency.domain.model.StationContact
import com.gec.emergency.domain.model.StationContactType

fun StationContactResponseDto.toDomainModel(): StationContact {
    val contactType = runCatching { StationContactType.valueOf(type) }.getOrDefault(StationContactType.OTHER)
    return StationContact(
        id = id,
        stationId = stationId,
        phoneNumber = phoneNumber,
        type = contactType,
        priority = priority,
        is24Hour = is24Hour,
        isPublic = true,
        isActive = active,
        isVerified = verified,
    )
}

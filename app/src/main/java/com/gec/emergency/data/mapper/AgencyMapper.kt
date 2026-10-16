package com.gec.emergency.data.mapper

import com.gec.emergency.data.remote.dto.AgencyResponseDto
import com.gec.emergency.domain.model.Agency
import com.gec.emergency.domain.model.AgencyType

fun AgencyResponseDto.toDomainModel(): Agency {
    val agencyType = runCatching { AgencyType.valueOf(type) }.getOrDefault(AgencyType.OTHER)
    return Agency(
        id = id,
        name = name,
        code = code,
        type = agencyType,
        emergencyNumber = emergencyNumber,
        isActive = active,
        isVerified = verified,
    )
}

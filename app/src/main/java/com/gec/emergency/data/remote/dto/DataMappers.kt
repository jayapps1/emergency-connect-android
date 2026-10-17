package com.gec.emergency.data.remote.dto

import com.gec.emergency.domain.model.Agency
import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.GeoLocation
import com.gec.emergency.domain.model.IncidentCategory
import com.gec.emergency.domain.model.IncidentReport
import com.gec.emergency.domain.model.IncidentSeverity
import com.gec.emergency.domain.model.ReportLocation
import com.gec.emergency.domain.model.ReportMode
import com.gec.emergency.domain.model.ReportStatus
import com.gec.emergency.domain.model.ReportStatusHistory
import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.model.StationContact
import com.gec.emergency.domain.model.StationContactType
import com.gec.emergency.domain.model.VehicleInfo

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

fun IncidentCategoryResponseDto.toDomainModel(): IncidentCategory {
    val severity = runCatching { IncidentSeverity.valueOf(defaultSeverity) }.getOrDefault(IncidentSeverity.MEDIUM)
    val supportedModes = mutableSetOf<ReportMode>()
    if (supportsSilent) supportedModes.add(ReportMode.SILENT)
    if (supportsGeneral) supportedModes.add(ReportMode.GENERAL)

    val mappedAgencies = defaultAgencyTypes.mapNotNull { typeStr ->
        runCatching { AgencyType.valueOf(typeStr) }.getOrNull()
    }.toSet()

    return IncidentCategory(
        id = id,
        code = code,
        name = name,
        description = description,
        severity = severity,
        reportModeSupported = supportedModes,
        defaultAgencyTypes = mappedAgencies,
        supportsCall = supportsCall,
        isActive = true,
        displayOrder = displayOrder,
    )
}

fun IncidentReport.toRequestDto(clientRequestId: String? = null): IncidentReportRequestDto {
    val locDto = location?.let { loc ->
        LocationDto(
            latitude = loc.geoLocation.latitude,
            longitude = loc.geoLocation.longitude,
            accuracyMeters = loc.geoLocation.accuracyMeters,
            landmark = loc.landmark,
            locality = loc.locality,
            district = loc.district,
            region = loc.region,
        )
    }

    val vehicleDto = vehicleInfo?.let { v ->
        VehicleInfoDto(
            registrationNumber = v.registrationNumber,
            vehicleType = v.vehicleType,
            colour = v.colour,
            directionOfTravel = v.directionOfTravel,
        )
    }

    return IncidentReportRequestDto(
        categoryId = categoryId,
        reportMode = mode.name,
        description = description,
        anonymousSessionId = anonymousSessionId,
        clientGeneratedRequestId = clientRequestId ?: id,
        location = locDto,
        vehicleInfo = vehicleDto,
    )
}

fun IncidentReportResponseDto.toDomainModel(): IncidentReport {
    return IncidentReport(
        id = id,
        publicReference = publicReference,
        categoryId = "",
        mode = runCatching { ReportMode.valueOf(mode) }.getOrDefault(ReportMode.GENERAL),
        createdAtMillis = System.currentTimeMillis(),
        severity = runCatching { IncidentSeverity.valueOf(severity) }.getOrDefault(IncidentSeverity.MEDIUM),
        status = runCatching { ReportStatus.valueOf(status) }.getOrDefault(ReportStatus.SUBMITTED),
        trackingToken = trackingToken,
    )
}

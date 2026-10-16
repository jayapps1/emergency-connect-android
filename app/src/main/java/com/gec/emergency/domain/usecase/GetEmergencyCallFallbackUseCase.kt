package com.gec.emergency.domain.usecase

import com.gec.emergency.core.common.EmergencyNumberConfig
import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.repository.OfflineEmergencyDirectoryRepository
import com.gec.emergency.domain.repository.StationRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class EmergencyCallTarget(
    val phoneNumber: String,
    val contactLabel: String,
    val tierDescription: String,
    val isNational: Boolean = false,
)

class GetEmergencyCallFallbackUseCase @Inject constructor(
    private val stationRepository: StationRepository,
    private val offlineDirectoryRepository: OfflineEmergencyDirectoryRepository,
) {
    suspend operator fun invoke(station: Station, agencyType: AgencyType = AgencyType.OTHER): EmergencyCallTarget {
        // Tier 1: Local Station Contact
        try {
            val stationContacts = stationRepository.getContactsForStation(station.id).firstOrNull()
            val preferredStationContact = stationContacts?.filter { it.isPublic && it.isActive && it.isVerified }
                ?.minByOrNull { it.priority }

            if (preferredStationContact != null) {
                return EmergencyCallTarget(
                    phoneNumber = preferredStationContact.phoneNumber,
                    contactLabel = station.name,
                    tierDescription = "Local Station Direct",
                )
            }
        } catch (_: Exception) {
            // Proceed to Tier 2 if local station fails
        }

        // Tier 2: Regional Headquarters (RHQ) Contact for SAME agency and SAME region
        try {
            val rhqContacts = offlineDirectoryRepository.getRegionalHeadquartersContacts(station.region).firstOrNull()
            val matchingRhqContact = rhqContacts?.filter {
                it.agencyId == station.agencyId || (agencyType != AgencyType.OTHER && it.agencyType == agencyType)
            }?.minByOrNull { it.priority }

            if (matchingRhqContact != null) {
                return EmergencyCallTarget(
                    phoneNumber = matchingRhqContact.phoneNumber,
                    contactLabel = matchingRhqContact.stationName,
                    tierDescription = "Regional Headquarters Fallback",
                )
            }
        } catch (_: Exception) {
            // Proceed to Tier 3 if RHQ fails
        }

        // Tier 3: Agency Short Emergency Code
        try {
            val shortCodeContacts = offlineDirectoryRepository.getContactsByAgencyType(agencyType).firstOrNull()
            val agencyShortCode = shortCodeContacts?.firstOrNull { it.agencyType == agencyType && it.phoneNumber != EmergencyNumberConfig.NATIONAL_EMERGENCY_NUMBER }

            if (agencyShortCode != null) {
                return EmergencyCallTarget(
                    phoneNumber = agencyShortCode.phoneNumber,
                    contactLabel = agencyShortCode.label,
                    tierDescription = "Agency Emergency Short Code",
                )
            }
        } catch (_: Exception) {
            // Proceed to Tier 4 if short code fails
        }

        // Tier 4: National Emergency Number (112)
        return EmergencyCallTarget(
            phoneNumber = EmergencyNumberConfig.NATIONAL_EMERGENCY_NUMBER,
            contactLabel = "National Emergency Service",
            tierDescription = "National Emergency Fallback (112)",
            isNational = true,
        )
    }
}

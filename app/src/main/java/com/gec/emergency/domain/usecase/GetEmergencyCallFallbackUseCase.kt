package com.gec.emergency.domain.usecase

import com.gec.emergency.core.common.EmergencyNumberConfig
import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.repository.OfflineEmergencyDirectoryRepository
import com.gec.emergency.domain.repository.StationRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Encapsulates the target phone number and fallback tier description for an emergency call.
 *
 * @property phoneNumber sanitized telephone or short code
 * @property contactLabel display label of the target contact or station
 * @property tierDescription human-readable fallback tier description
 * @property isNational true if target is the primary national emergency number (112)
 */
data class EmergencyCallTarget(
    val phoneNumber: String,
    val contactLabel: String,
    val tierDescription: String,
    val isNational: Boolean = false,
)

/**
 * Domain use case encapsulating the 4-tier Emergency Call Fallback Hierarchy across Ghana Emergency Connect.
 *
 * <p>Fallback Hierarchy:
 * <ol>
 *   <li><b>Tier 1 (Preferred Local Station):</b> First public, active, and verified contact for the selected station
 *       ordered by priority ASC.</li>
 *   <li><b>Tier 2 (Same Agency Regional HQ):</b> Public verified Regional Headquarters (RHQ) contact matching the
 *       <b>SAME agency ID</b> and <b>SAME region name</b>.</li>
 *   <li><b>Tier 3 (Agency Emergency Short Code):</b> Configured agency emergency short code (e.g., 192 for Fire,
 *       191 for Police, 193 for Ambulance).</li>
 *   <li><b>Tier 4 (National Emergency Fallback):</b> Primary National Emergency Service number (112).</li>
 * </ol>
 */
class GetEmergencyCallFallbackUseCase @Inject constructor(
    private val stationRepository: StationRepository,
    private val offlineDirectoryRepository: OfflineEmergencyDirectoryRepository,
) {
    /**
     * Resolves the highest-priority eligible call target for an emergency station request.
     *
     * @param station the target emergency station
     * @param agencyType the agency classification
     * @return resolved {@link EmergencyCallTarget}
     */
    suspend operator fun invoke(station: Station, agencyType: AgencyType = AgencyType.OTHER): EmergencyCallTarget {
        // Tier 1: Preferred Local Station Contact
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
            // Proceed gracefully to Tier 2 if local station contact is unavailable
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
            // Proceed gracefully to Tier 3 if RHQ is unavailable
        }

        // Tier 3: Agency Emergency Short Code
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
            // Proceed gracefully to Tier 4 if short code is unavailable
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

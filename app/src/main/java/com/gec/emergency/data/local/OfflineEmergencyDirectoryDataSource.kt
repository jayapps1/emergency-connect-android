package com.gec.emergency.data.local

import com.gec.emergency.core.common.EmergencyNumberConfig
import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.OfflineEmergencyContact
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bundled offline emergency contact data source.
 *
 * Provides essential emergency fallback numbers available 100% offline
 * without network connection or backend dependencies.
 */
@Singleton
class OfflineEmergencyDirectoryDataSource @Inject constructor() {

    private val contacts = listOf(
        OfflineEmergencyContact(
            id = "off_nat_112",
            agencyType = AgencyType.OTHER,
            label = "National Emergency Number",
            phoneNumber = EmergencyNumberConfig.NATIONAL_EMERGENCY_NUMBER,
            priority = 1,
            isNational = true,
            isActive = true,
        ),
        OfflineEmergencyContact(
            id = "off_gnfs_192",
            agencyType = AgencyType.FIRE,
            label = "Ghana National Fire Service",
            phoneNumber = "192",
            priority = 2,
            isNational = true,
            isActive = true,
        ),
        OfflineEmergencyContact(
            id = "off_gps_191",
            agencyType = AgencyType.POLICE,
            label = "Ghana Police Service",
            phoneNumber = "191",
            priority = 2,
            isNational = true,
            isActive = true,
        ),
        OfflineEmergencyContact(
            id = "off_nas_193",
            agencyType = AgencyType.AMBULANCE,
            label = "National Ambulance Service",
            phoneNumber = "193",
            priority = 2,
            isNational = true,
            isActive = true,
        ),
        OfflineEmergencyContact(
            id = "off_nadmo",
            agencyType = AgencyType.DISASTER_MANAGEMENT,
            label = "NADMO National Headquarters",
            phoneNumber = "+233-302-688515",
            priority = 3,
            isNational = true,
            isActive = true,
        ),
    )

    fun getContacts(): List<OfflineEmergencyContact> = contacts
}

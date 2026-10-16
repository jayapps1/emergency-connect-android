package com.gec.emergency.domain

import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.OfflineEmergencyContact
import com.gec.emergency.domain.repository.OfflineEmergencyDirectoryRepository
import com.gec.emergency.domain.usecase.GetOfflineEmergencyContactsUseCase
import com.gec.emergency.domain.usecase.GetPrimaryNationalEmergencyContactUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class GetPrimaryNationalEmergencyContactUseCaseTest {

    private val fakeRepository = object : OfflineEmergencyDirectoryRepository {
        override fun getEmergencyContacts(): Flow<List<OfflineEmergencyContact>> {
            return flowOf(
                listOf(
                    OfflineEmergencyContact(
                        id = "off_nat_112",
                        agencyType = AgencyType.OTHER,
                        label = "National Emergency Number",
                        phoneNumber = "112",
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
                )
            )
        }

        override fun getPrimaryNationalEmergencyContact(): Flow<OfflineEmergencyContact?> {
            return flowOf(
                OfflineEmergencyContact(
                    id = "off_nat_112",
                    agencyType = AgencyType.OTHER,
                    label = "National Emergency Number",
                    phoneNumber = "112",
                    priority = 1,
                    isNational = true,
                    isActive = true,
                )
            )
        }

        override fun getContactsByAgencyType(type: AgencyType): Flow<List<OfflineEmergencyContact>> {
            return flowOf(emptyList())
        }
    }

    private val primaryUseCase = GetPrimaryNationalEmergencyContactUseCase(fakeRepository)
    private val offlineContactsUseCase = GetOfflineEmergencyContactsUseCase(fakeRepository)

    @Test
    fun getPrimaryNational_returns112Contact() = runBlocking {
        val result = primaryUseCase().first()

        assertNotNull(result)
        assertEquals("112", result?.phoneNumber)
        assertEquals(1, result?.priority)
    }

    @Test
    fun getOfflineContacts_returnsContactsSortedByPriority() = runBlocking {
        val result = offlineContactsUseCase().first()

        assertEquals(2, result.size)
        assertEquals("off_nat_112", result[0].id)
        assertEquals("off_gnfs_192", result[1].id)
    }
}

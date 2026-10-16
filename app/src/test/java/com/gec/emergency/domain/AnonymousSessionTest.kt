package com.gec.emergency.domain

import com.gec.emergency.domain.model.AnonymousSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class AnonymousSessionTest {

    @Test
    fun anonymousSession_validInstallationUuid_createsSuccessfully() {
        val uuid = UUID.randomUUID().toString()
        val session = AnonymousSession(
            id = uuid,
            installationScoped = true,
        )

        assertNotNull(session.id)
        assertEquals(uuid, session.id)
        assertTrue(session.installationScoped)
    }
}

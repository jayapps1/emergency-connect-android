package com.gec.emergency.core.calling

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android system implementation of {@link CallManager} invoking the native dialer application.
 *
 * <p>Safety Rationale:
 * ACTION_DIAL is used intentionally instead of ACTION_CALL. This leaves final call initiation
 * to explicit user confirmation, avoids requiring direct CALL_PHONE runtime permissions, and prevents
 * unauthorized or accidental emergency dialling.
 */
@Singleton
class AndroidCallManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : CallManager {

    /**
     * Sanitizes the input phone number and launches the system dialer with a {@code tel:} URI.
     *
     * @param request call parameters containing destination phone number and call type
     * @return {@link CallResult} status
     */
    override suspend fun placeCall(request: CallRequest): CallResult {
        val sanitizedNumber = PhoneNumberSanitizer.sanitize(request.phoneNumber)
            ?: return CallResult.InvalidNumber

        return try {
            val uri = Uri.parse("tel:$sanitizedNumber")
            val intent = Intent(Intent.ACTION_DIAL, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            CallResult.Started
        } catch (_: ActivityNotFoundException) {
            CallResult.Unsupported
        } catch (e: SecurityException) {
            CallResult.Failed(e.localizedMessage)
        } catch (e: Exception) {
            CallResult.Failed(e.localizedMessage)
        }
    }
}

package com.gec.emergency.core.calling

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android system implementation of {@link CallManager} initiating direct phone calls.
 *
 * <p>Direct calling occurs only after explicit citizen confirmation in GEC confirmation dialogs
 * and Android CALL_PHONE permission verification. GEC never bypasses platform permission controls.
 */
@Singleton
class AndroidCallManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : CallManager {

    /**
     * Sanitizes the input phone number and initiates a call using ACTION_CALL if CALL_PHONE permission
     * is granted, falling back gracefully to ACTION_DIAL if permission is not granted.
     *
     * @param request call parameters containing destination phone number and call type
     * @return {@link CallResult} status
     */
    override suspend fun placeCall(request: CallRequest): CallResult {
        val sanitizedNumber = PhoneNumberSanitizer.sanitize(request.phoneNumber)
            ?: return CallResult.InvalidNumber

        val uri = Uri.parse("tel:$sanitizedNumber")
        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE,
        ) == PackageManager.PERMISSION_GRANTED

        val action = if (hasCallPermission) Intent.ACTION_CALL else Intent.ACTION_DIAL

        return try {
            val intent = Intent(action, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CallResult.Started
        } catch (_: ActivityNotFoundException) {
            CallResult.Unsupported
        } catch (e: SecurityException) {
            // Fallback to dialer if direct call security exception occurs
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
                CallResult.Started
            } catch (_: Exception) {
                CallResult.Failed(e.localizedMessage)
            }
        } catch (e: Exception) {
            CallResult.Failed(e.localizedMessage)
        }
    }
}

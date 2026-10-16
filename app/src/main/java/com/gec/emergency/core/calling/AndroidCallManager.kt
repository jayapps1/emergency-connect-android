package com.gec.emergency.core.calling

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidCallManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : CallManager {

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

package com.github.kr328.clash.remote

import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.github.kr328.clash.common.constants.Authorities
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.service.StatusProvider

class StatusClient(private val context: Context) {
    private val uri: Uri
        get() {
            return Uri.Builder()
                .scheme("content")
                .authority(Authorities.STATUS_PROVIDER)
                .build()
        }

    fun isServiceRunning(): Boolean {
        return queryStatus() != null
    }

    fun currentProfile(): String? {
        return queryStatus()?.getString("name")
    }

    private fun queryStatus(): Bundle? {
        return try {
            context.contentResolver.call(
                uri,
                StatusProvider.METHOD_CURRENT_PROFILE,
                null,
                null,
            )
        } catch (e: Exception) {
            Log.w("Query current profile: $e", e)

            null
        }
    }
}
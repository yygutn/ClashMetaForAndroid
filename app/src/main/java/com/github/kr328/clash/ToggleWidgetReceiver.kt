package com.github.kr328.clash

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.remote.StatusClient
import com.github.kr328.clash.util.startClashService
import com.github.kr328.clash.util.stopClashService

class ToggleWidgetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intents.ACTION_WIDGET_TOGGLE -> handleToggle(context)
            Intents.ACTION_CLASH_STARTED,
            Intents.ACTION_CLASH_STOPPED,
            Intents.ACTION_PROFILE_LOADED,
            Intents.ACTION_SERVICE_RECREATED,
            -> ToggleWidgetProvider.updateAll(context)
        }
    }

    private fun handleToggle(context: Context) {
        val status = StatusClient(context)

        if (status.isServiceRunning()) {
            context.stopClashService()
            ToggleWidgetProvider.updateAll(context)
            return
        }

        val vpnRequest = context.startClashService()
        if (vpnRequest != null) {
            vpnRequest.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(vpnRequest)
        }

        ToggleWidgetProvider.updateAll(context)
    }
}

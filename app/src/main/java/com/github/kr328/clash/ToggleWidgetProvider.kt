package com.github.kr328.clash

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.design.R as DesignR
import com.github.kr328.clash.remote.StatusClient
import com.github.kr328.clash.service.R as ServiceR
import com.github.kr328.clash.util.startClashService
import com.github.kr328.clash.util.stopClashService

class ToggleWidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intents.ACTION_WIDGET_TOGGLE -> {
                handleToggle(context)
                updateAll(context)
                return
            }
            Intents.ACTION_CLASH_STARTED,
            Intents.ACTION_CLASH_STOPPED,
            Intents.ACTION_PROFILE_LOADED,
            Intents.ACTION_SERVICE_RECREATED,
            -> {
                updateAll(context)
                return
            }
        }

        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun handleToggle(context: Context) {
        val status = StatusClient(context)

        if (status.isServiceRunning()) {
            context.stopClashService()
            return
        }

        val vpnRequest = context.startClashService()
        if (vpnRequest != null) {
            vpnRequest.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(vpnRequest)
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, ToggleWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)

            if (ids.isEmpty())
                return

            ToggleWidgetProvider().onUpdate(context, manager, ids)
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val status = StatusClient(context)
            val running = status.isServiceRunning()
            val profile = status.currentProfile()

            val views = RemoteViews(context.packageName, R.layout.widget_toggle)

            views.setImageViewResource(R.id.widget_icon, ServiceR.drawable.ic_logo_service)
            views.setTextViewText(R.id.widget_title, context.getText(R.string.launch_name))
            views.setTextViewText(
                R.id.widget_subtitle,
                when {
                    running && !profile.isNullOrEmpty() -> profile
                    running -> context.getText(DesignR.string.running)
                    else -> context.getText(DesignR.string.stopped)
                },
            )

            val toggleIntent = Intent(context, ToggleWidgetProvider::class.java).apply {
                action = Intents.ACTION_WIDGET_TOGGLE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                views.setCompoundButtonChecked(R.id.widget_switch, running)
                views.setOnCheckedChangeResponse(
                    R.id.widget_switch,
                    RemoteViews.RemoteResponse.fromPendingIntent(pendingIntent),
                )
            } else {
                views.setOnClickPendingIntent(R.id.widget_switch, pendingIntent)
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

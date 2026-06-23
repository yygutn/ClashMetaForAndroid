package com.github.kr328.clash.util

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.github.kr328.clash.ExternalControlActivity
import com.github.kr328.clash.R
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.design.R as DesignR

object ClashShortcut {
    private fun externalFlags(): Int {
        return Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
            Intent.FLAG_ACTIVITY_NO_ANIMATION
    }

    fun buildToggleShortcut(context: Context): ShortcutInfoCompat {
        return ShortcutInfoCompat.Builder(context, "toggle_clash")
            .setShortLabel(context.getString(DesignR.string.shortcut_toggle_short))
            .setLongLabel(context.getString(DesignR.string.shortcut_toggle_long))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_toggle_all))
            .setIntent(
                Intent(Intents.ACTION_TOGGLE_CLASH)
                    .setClassName(context, ExternalControlActivity::class.java.name)
                    .addFlags(externalFlags()),
            )
            .build()
    }

    fun buildStartShortcut(context: Context): ShortcutInfoCompat {
        return ShortcutInfoCompat.Builder(context, "start_clash")
            .setShortLabel(context.getString(DesignR.string.shortcut_start_short))
            .setLongLabel(context.getString(DesignR.string.shortcut_start_long))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_toggle_on))
            .setIntent(
                Intent(Intents.ACTION_START_CLASH)
                    .setClassName(context, ExternalControlActivity::class.java.name)
                    .addFlags(externalFlags()),
            )
            .build()
    }

    fun buildStopShortcut(context: Context): ShortcutInfoCompat {
        return ShortcutInfoCompat.Builder(context, "stop_clash")
            .setShortLabel(context.getString(DesignR.string.shortcut_stop_short))
            .setLongLabel(context.getString(DesignR.string.shortcut_stop_long))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_toggle_off))
            .setIntent(
                Intent(Intents.ACTION_STOP_CLASH)
                    .setClassName(context, ExternalControlActivity::class.java.name)
                    .addFlags(externalFlags()),
            )
            .build()
    }

    fun publishDynamicShortcuts(context: Context) {
        ShortcutManagerCompat.setDynamicShortcuts(
            context,
            listOf(
                buildToggleShortcut(context),
                buildStartShortcut(context),
                buildStopShortcut(context),
            ),
        )
    }

    fun isPinShortcutSupported(context: Context): Boolean {
        return ShortcutManagerCompat.isRequestPinShortcutSupported(context)
    }

    fun requestPinToggle(context: Context): Boolean {
        if (!isPinShortcutSupported(context))
            return false

        return ShortcutManagerCompat.requestPinShortcut(
            context,
            buildToggleShortcut(context),
            null,
        )
    }

    fun widgetProviderCount(context: Context): Int {
        val manager = AppWidgetManager.getInstance(context)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.getInstalledProvidersForPackage(context.packageName, Process.myUserHandle()).size
        } else {
            @Suppress("DEPRECATION")
            manager.installedProviders.count { it.provider.packageName == context.packageName }
        }
    }
}

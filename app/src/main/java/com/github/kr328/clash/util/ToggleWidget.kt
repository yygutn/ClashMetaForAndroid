package com.github.kr328.clash.util

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import com.github.kr328.clash.ToggleWidgetProvider

object ToggleWidget {
    fun isPinSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return false

        return AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported
    }

    fun requestPin(context: Context): Boolean {
        if (!isPinSupported(context))
            return false

        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, ToggleWidgetProvider::class.java)

        return manager.requestPinAppWidget(component, null, null)
    }
}

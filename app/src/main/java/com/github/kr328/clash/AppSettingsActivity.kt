package com.github.kr328.clash

import android.content.ComponentName
import android.content.pm.PackageManager
import android.widget.Toast
import com.github.kr328.clash.common.util.componentName
import com.github.kr328.clash.design.AppSettingsDesign
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.model.Behavior
import com.github.kr328.clash.design.store.UiStore.Companion.mainActivityAlias
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.util.ApplicationObserver
import com.github.kr328.clash.util.ClashShortcut
import com.github.kr328.clash.util.ToggleWidget
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select

class AppSettingsActivity : BaseActivity<AppSettingsDesign>(), Behavior {
    override suspend fun main() {
        val design = AppSettingsDesign(
            this,
            uiStore,
            ServiceStore(this),
            this,
            clashRunning,
            ::onHideIconChange,
            ClashShortcut.widgetProviderCount(this),
            ::onPinWidget,
            ::onPinShortcut,
        )

        setContentDesign(design)

        while (isActive) {
            select<Unit> {
                events.onReceive {
                    when (it) {
                        Event.ClashStart, Event.ClashStop, Event.ServiceRecreated ->
                            recreate()
                        else -> Unit
                    }
                }
                design.requests.onReceive {
                    ApplicationObserver.createdActivities.forEach {
                        it.recreate()
                    }
                }
            }
        }
    }

    override var autoRestart: Boolean
        get() {
            val status = packageManager.getComponentEnabledSetting(
                RestartReceiver::class.componentName
            )

            return status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
        set(value) {
            val status = if (value)
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED

            packageManager.setComponentEnabledSetting(
                RestartReceiver::class.componentName,
                status,
                PackageManager.DONT_KILL_APP,
            )
        }

    private fun onHideIconChange(hide: Boolean) {
        val newState = if (hide) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
        packageManager.setComponentEnabledSetting(
            mainActivityAlias,
            newState,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun onPinWidget() {
        val message = when {
            ToggleWidget.requestPin(this) -> R.string.widget_pin_requested
            ToggleWidget.isPinSupported(this) -> R.string.widget_pin_failed
            else -> R.string.widget_pin_unsupported
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun onPinShortcut() {
        val message = when {
            ClashShortcut.requestPinToggle(this) -> R.string.shortcut_pin_requested
            ClashShortcut.isPinShortcutSupported(this) -> R.string.shortcut_pin_failed
            else -> R.string.shortcut_pin_unsupported
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

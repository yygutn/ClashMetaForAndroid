package com.github.kr328.clash.design.store

import android.content.Context
import com.github.kr328.clash.core.model.Proxy
import java.util.UUID

class ProxyDelayStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun merge(profile: UUID, group: String, proxies: List<Proxy>): List<Proxy> {
        if (proxies.isEmpty()) return proxies

        return proxies.map { proxy ->
            val cached = get(profile, group, proxy.name)

            if (proxy.delay in 1..Short.MAX_VALUE) {
                proxy
            } else if (cached != null) {
                proxy.copy(delay = cached)
            } else {
                proxy
            }
        }
    }

    fun saveGroup(profile: UUID, group: String, proxies: List<Proxy>) {
        val editor = prefs.edit()
        var changed = false

        proxies.forEach { proxy ->
            if (proxy.delay in 1..Short.MAX_VALUE) {
                editor.putInt(key(profile, group, proxy.name), proxy.delay)
                changed = true
            }
        }

        if (changed) {
            editor.apply()
        }
    }

    fun clearProfile(profile: UUID) {
        val prefix = "$profile|"
        val editor = prefs.edit()
        var changed = false

        prefs.all.keys.forEach { storedKey ->
            if (storedKey.startsWith(prefix)) {
                editor.remove(storedKey)
                changed = true
            }
        }

        if (changed) {
            editor.apply()
        }
    }

    private fun get(profile: UUID, group: String, proxy: String): Int? {
        val delay = prefs.getInt(key(profile, group, proxy), -1)
        return delay.takeIf { it in 1..Short.MAX_VALUE }
    }

    private fun key(profile: UUID, group: String, proxy: String): String {
        return "$profile|$group|$proxy"
    }

    companion object {
        private const val PREFERENCE_NAME = "proxy_delays"
    }
}

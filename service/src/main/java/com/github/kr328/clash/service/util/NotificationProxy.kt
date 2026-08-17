package com.github.kr328.clash.service.util

import android.content.Context
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.model.ProxySort
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.service.R

object NotificationProxy {
    private const val UI_PREFERENCE_NAME = "ui"
    private const val PROXY_LAST_GROUP_KEY = "proxy_last_group"

    private val PREFERRED_GROUPS = listOf(
        "PROXY",
        "Proxy",
        "节点选择",
        "代理",
    )

    fun formatTitle(profileName: String, node: String): String {
        return "$profileName | $node"
    }

    // 目标组解析缓存：查找 Selector 类型组可能需要逐组全量查询（JNI + JSON 反序列化），
    // 订阅组多时开销大。key 为 组名列表 + 最近浏览的组，任一变化才重新解析。
    @Volatile
    private var cachedResolveKey: List<String?>? = null

    @Volatile
    private var cachedTargetGroup: String? = null

    fun resolveCurrentNode(context: Context): String {
        return when (Clash.queryTunnelState().mode) {
            TunnelState.Mode.Direct -> context.getString(R.string.direct_mode)
            TunnelState.Mode.Global -> resolveGroupNode("GLOBAL")
            else -> {
                val names = Clash.queryGroupNames(excludeNotSelectable = true)
                val groupName = resolveTargetGroup(context, names) ?: return "?"
                resolveGroupNode(groupName)
            }
        }
    }

    private fun resolveTargetGroup(context: Context, names: List<String>): String? {
        if (names.isEmpty()) return null

        val prefs = context.getSharedPreferences(UI_PREFERENCE_NAME, Context.MODE_PRIVATE)
        val lastGroup = prefs.getString(PROXY_LAST_GROUP_KEY, null)

        val key = listOf(lastGroup) + names
        if (key == cachedResolveKey) {
            return cachedTargetGroup
        }

        val resolved = resolveTargetGroupUncached(names, lastGroup)

        cachedResolveKey = key
        cachedTargetGroup = resolved

        return resolved
    }

    private fun resolveTargetGroupUncached(names: List<String>, lastGroup: String?): String? {
        if (!lastGroup.isNullOrBlank() && lastGroup in names) {
            return lastGroup
        }

        PREFERRED_GROUPS.firstOrNull { it in names }?.let { return it }

        names.firstOrNull { name ->
            Clash.queryGroup(name, ProxySort.Default).type == "Selector"
        }?.let { return it }

        return names.firstOrNull()
    }

    private fun resolveGroupNode(groupName: String): String {
        var current = groupName

        repeat(8) {
            val group = Clash.queryGroup(current, ProxySort.Default)
            if (group.type == "Unknown" || group.now.isBlank()) {
                return current
            }

            val selected = group.proxies.find { it.name == group.now }
            if (selected == null || !selected.isGroup) {
                return group.now
            }

            current = group.now
        }

        return current
    }
}

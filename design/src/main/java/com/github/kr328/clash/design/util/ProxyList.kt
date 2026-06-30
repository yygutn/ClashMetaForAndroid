package com.github.kr328.clash.design.util

import com.github.kr328.clash.core.model.Proxy
import com.github.kr328.clash.core.model.ProxySort

fun List<Proxy>.sortedByProxySort(sort: ProxySort): List<Proxy> {
    return when (sort) {
        ProxySort.Title -> sortedBy { it.title }
        ProxySort.Delay -> sortedWith(
            compareBy<Proxy> { proxy ->
                if (proxy.delay in 1..Short.MAX_VALUE) proxy.delay else Int.MAX_VALUE
            }.thenBy { it.name }
        )
        ProxySort.Default -> this
    }
}

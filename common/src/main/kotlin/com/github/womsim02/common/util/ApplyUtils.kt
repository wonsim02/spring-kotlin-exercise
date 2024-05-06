package com.github.womsim02.common.util

inline fun <T, V : Any> T.applyIfNotNull(
    value: V?,
    applyBlock: T.(V) -> Unit,
): T {
    if (value != null) { applyBlock(value) }
    return this
}

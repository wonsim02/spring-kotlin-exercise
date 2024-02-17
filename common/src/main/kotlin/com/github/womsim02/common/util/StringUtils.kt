package com.github.womsim02.common.util

fun String.toCamelCase(): String {
    return if (contains("_")) {
        // "_"로 분리
        val substrings = this
            .split("_")
            .filterNot { it.isEmpty() }
            .withIndex()

        // 맨 처음 substring은 전부 소문자로, 그 이후 substring은 첫 문자만 대문자, 나머지는 소문자로
        substrings.joinToString("") { (index, substring) ->
            if (index == 0) {
                substring.lowercase()
            } else {
                substring.substring(0, 1).uppercase() +
                    substring.substring(1).lowercase()
            }
        }
    } else {
        // 첫 문자만 소문자로 변경
        this.substring(0, 1).lowercase() + this.substring(1)
    }
}

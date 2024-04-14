package com.github.wonsim02.infra.jpa.util

import com.vladmihalcea.hibernate.type.array.ListArrayType
import org.hibernate.dialect.function.StandardSQLFunction

/**
 * @see <a href="https://www.postgresql.org/docs/11/functions-array.html">
 *     Array Functions and Operators
 *     </a>
 */
class ArrayAppendFunction : StandardSQLFunction(NAME, ListArrayType.INSTANCE) {

    companion object {

        const val NAME = "array_append"
        const val TEMPLATE = "$NAME({0], {1})"
    }
}

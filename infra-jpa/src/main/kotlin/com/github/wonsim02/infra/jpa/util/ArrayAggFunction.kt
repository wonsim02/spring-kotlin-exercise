package com.github.wonsim02.infra.jpa.util

import com.vladmihalcea.hibernate.type.array.ListArrayType
import org.hibernate.dialect.function.StandardSQLFunction

/**
 * @see <a href="https://www.postgresql.org/docs/11/functions-aggregate.html">
 *     Aggregate Functions
 *     </a>
 */
class ArrayAggFunction : StandardSQLFunction(NAME, ListArrayType.INSTANCE) {

    companion object {

        const val NAME = "array_agg"
        const val TEMPLATE = "$NAME({0})"
    }
}

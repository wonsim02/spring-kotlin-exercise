package com.github.wonsim02.infra.jpa.util

import com.github.wonsim02.infra.jpa.config.CustomPostgresDialect
import com.querydsl.core.types.Template
import com.querydsl.core.types.TemplateFactory

/**
 * [CustomPostgresDialect]에 등록된 SQL 함수에 대한 QueryDSL [Template]을 생성하는 유틸.
 */
object CustomSqlFunctionTemplates {

    fun arrayAgg(): Template {
        return TemplateFactory.DEFAULT.create(ArrayAggFunction.TEMPLATE)
    }

    fun arrayAppend(): Template {
        return TemplateFactory.DEFAULT.create(ArrayAppendFunction.TEMPLATE)
    }
}

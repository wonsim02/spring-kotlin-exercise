package com.github.wonsim02.infra.jpa.config

import com.github.wonsim02.infra.jpa.util.ArrayAggFunction
import com.github.wonsim02.infra.jpa.util.ArrayAppendFunction
import org.hibernate.dialect.PostgreSQL10Dialect

/**
 * Hibernate를 통해 JPA 쿼리를 변환할 때 [PostgreSQL10Dialect]에서 기본적으로 지원하지 않는 함수를 인식할 수 있도록 하는 dialect.
 * 현재 사용 중인 Hiberante 버전에서는 Postgres 데이터베이스에 대해서 [PostgreSQL10Dialect]가 최신이다.
 * @see <a href="https://stackoverflow.com/questions/67768125/how-to-use-postgresql-array-agg-function-in-jparepository-spring-boot">
 *     Stack Overflow - how to use postgresql array_agg function in JpaRepository spring boot?
 *     </a>
 * @see org.hibernate.dialect.Database.POSTGRESQL
 */
class CustomPostgresDialect : PostgreSQL10Dialect() {

    init {
        this.registerFunction(ArrayAggFunction.NAME, ArrayAggFunction())
        this.registerFunction(ArrayAppendFunction.NAME, ArrayAppendFunction())
    }
}

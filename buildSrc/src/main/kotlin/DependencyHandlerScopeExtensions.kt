import org.gradle.kotlin.dsl.DependencyHandlerScope

fun DependencyHandlerScope.flyway(
    module: String,
    version: String = Ver.flyway,
): String = "org.flywaydb:flyway-$module:$version"

fun DependencyHandlerScope.hibernate(
    module: String,
): String = "org.hibernate:hibernate-$module"

fun DependencyHandlerScope.hibernateTypes(
    version: String = Ver.hibernateTypes,
): String = "com.vladmihalcea:hibernate-types-$version"

fun DependencyHandlerScope.kotlinxSerialization(
    module: String,
    version: String = Ver.kotlinxSerialization,
) = "org.jetbrains.kotlinx:kotlinx-serialization-$module:$version"

fun DependencyHandlerScope.okhttp(
    module: String,
    version: String = Ver.okhttp,
): String = "com.squareup.okhttp3:$module:$version"

fun DependencyHandlerScope.postgres(
    version: String = Ver.postgres,
): String = "org.postgresql:postgresql:$version"

fun DependencyHandlerScope.queryDsl(
    module: String,
    version: String = Ver.queryDsl,
) = "com.querydsl:$module:$version"

fun DependencyHandlerScope.spring(
    module: String,
): String = "org.springframework:spring-$module"

fun DependencyHandlerScope.springBoot(
    module: String,
    version: String = Ver.springBoot,
): String = "org.springframework.boot:spring-boot-$module:$version"

fun DependencyHandlerScope.testContainersBom(
    version: String = Ver.testContainers,
): String = "org.testcontainers:testcontainers-bom:$version"

fun DependencyHandlerScope.testContainers(
    module: String,
    version: String = Ver.testContainers,
): String = "org.testcontainers:$module:$version"

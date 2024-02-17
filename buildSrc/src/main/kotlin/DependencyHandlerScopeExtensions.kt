import org.gradle.kotlin.dsl.DependencyHandlerScope

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

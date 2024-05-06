allOpen {
    annotation("javax.persistence.Entity")
}

dependencies {
    implementation(springBoot("starter-web"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springdoc:springdoc-openapi-ui:1.7.0")

    implementation(project(":common"))
    implementation(project(":infra-jpa"))
    implementation(project(":infra-redis"))

    implementation(queryDsl("querydsl-core"))
    implementation(queryDsl("querydsl-jpa"))
    kapt(queryDsl("querydsl-apt", version = "${Ver.queryDsl}:jpa"))

    testImplementation(retrofit("retrofit"))
    testImplementation(retrofit("converter-jackson"))
    testImplementation(testFixtures(project(":infra-jpa")))
    testImplementation(testFixtures(project(":infra-redis")))
}

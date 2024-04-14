dependencies {
    api(springBoot("starter-data-jpa"))
    runtimeOnly(flyway("core"))
    api(hibernate("core"))
    api(hibernateTypes())
    implementation(postgres())
    implementation(queryDsl("querydsl-core"))
    implementation(queryDsl("querydsl-jpa"))
    implementation(project(":common"))

    testImplementation(queryDsl("querydsl-apt"))
    kaptTest(queryDsl("querydsl-apt", version = "${Ver.queryDsl}:jpa"))

    testFixturesApi(platform(testContainersBom()))
    testFixturesApi(springBoot("starter-test"))
    testFixturesApi(testContainers("junit-jupiter"))
    testFixturesApi(testContainers("postgresql"))
}

dependencies {
    api(springBoot("starter-data-jpa"))
    runtimeOnly(flyway("core"))
    api(hibernate("core"))
    api(hibernateTypes())
    implementation(postgres())
    implementation(project(":common"))

    testFixturesApi(platform(testContainersBom()))
    testFixturesApi(springBoot("starter-test"))
    testFixturesApi(testContainers("junit-jupiter"))
    testFixturesApi(testContainers("postgresql"))
}

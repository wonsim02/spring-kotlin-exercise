dependencies {
    api(springBoot("starter-data-jpa"))
    runtimeOnly(flyway("core"))
    runtimeOnly(postgres())
    implementation(project(":common"))

    testFixturesApi(platform(testContainersBom()))
    testFixturesApi(springBoot("starter-test"))
    testFixturesApi(testContainers("junit-jupiter"))
    testFixturesApi(testContainers("postgresql"))
}

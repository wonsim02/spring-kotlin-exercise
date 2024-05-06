dependencies {
    api(springBoot("starter-data-redis"))
    implementation(project(":common"))

    testFixturesApi(platform(testContainersBom()))
    testFixturesApi(springBoot("starter-test"))
    testFixturesApi(testContainers("junit-jupiter"))
}

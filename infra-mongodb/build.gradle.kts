dependencies {
    api(springBoot("starter-data-mongodb"))
    implementation(project(":common"))

    testImplementation(platform(testContainersBom()))
    testImplementation(springBoot("starter-test"))
    testImplementation(testContainers("junit-jupiter"))
    testImplementation(testContainers("mongodb"))
    kaptTest(project(":infra-mongodb"))
}

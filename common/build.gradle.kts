dependencies {
    implementation(spring("context"))
    implementation(springBoot("actuator"))

    testImplementation(springBoot("starter-test"))
    testImplementation(springBoot("starter-actuator"))
}

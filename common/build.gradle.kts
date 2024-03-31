plugins {
    id(Plugins.kotlinSerialization) version Ver.kotlin
}

dependencies {
    implementation(spring("context"))
    implementation(springBoot("actuator"))

    testImplementation(kotlinxSerialization("json"))
    testImplementation(okhttp("okhttp"))
    testImplementation(springBoot("starter-test"))
    testImplementation(springBoot("starter-actuator"))
    testImplementation(springBoot("starter-web"))
}

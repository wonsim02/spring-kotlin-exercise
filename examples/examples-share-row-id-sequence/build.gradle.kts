dependencies {
    implementation(project(":infra-jpa"))
    implementation(queryDsl("querydsl-core"))
    implementation(queryDsl("querydsl-jpa"))
    kapt(queryDsl("querydsl-apt", version = "${Ver.queryDsl}:jpa"))

    testImplementation(testFixtures(project(":infra-jpa")))
    testImplementation(queryDsl("querydsl-apt"))
}

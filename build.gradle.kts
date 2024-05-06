import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    idea
    `java-test-fixtures`
    kotlin("plugin.allopen") version Ver.kotlin
    kotlin("jvm") version Ver.kotlin
    kotlin("kapt") version Ver.kotlin

    id(Plugins.kotlinJpa) version Ver.kotlin
    id(Plugins.kotlinSpring) version Ver.kotlin
    id(Plugins.ktLint) version Ver.ktlintPlugin
    id(Plugins.ktLintIdea) version Ver.ktlintPlugin
    id(Plugins.springBoot) version Ver.springBoot apply false
    id(Plugins.springDependencyManagement) version Ver.springDependencyManagement
}

allprojects {
    repositories {
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
        mavenCentral()
    }
}

group = "com.github.wonsim02"
version = "0.0.1-SNAPSHOT"

subprojects {
    group = rootProject.group
    version = rootProject.version

    apply(plugin = Plugins.idea)
    apply(plugin = Plugins.javaTestFixtures)
    apply(plugin = Plugins.kotlin)
    apply(plugin = Plugins.kotlinJpa)
    apply(plugin = Plugins.kotlinKapt)
    apply(plugin = Plugins.kotlinSpring)
    apply(plugin = Plugins.ktLint)
    apply(plugin = Plugins.ktLintIdea)
    apply(plugin = Plugins.springDependencyManagement)

    configure<DependencyManagementExtension> {
        imports {
            mavenBom("org.jetbrains.kotlin:kotlin-bom:${Ver.kotlin}")
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_15
        targetCompatibility = JavaVersion.VERSION_15
    }

    configure<KtlintExtension> {
        version.set(Ver.ktlint)
        disabledRules.add("filename")
    }

    dependencies {
        implementation(kotlin("stdlib"))
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))

        // https://docs.spring.io/spring-boot/docs/2.7.7/reference/html/configuration-metadata.html#appendix.configuration-metadata.annotation-processor
        annotationProcessor(springBoot("configuration-processor"))
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs += "-Xjsr305=strict"
                jvmTarget = "15"
            }
        }

        withType<Test> {
            useJUnitPlatform()
        }
    }
}

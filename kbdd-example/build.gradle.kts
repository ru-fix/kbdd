plugins {
    java
    kotlin("jvm")
    id("io.qameta.allure") version Vers.allure_plugin
}

allure{
    adapter{
        version.set(Vers.allure_cli)
        allureJavaVersion.set(Vers.allure_java)

        frameworks{
            junit5{
                enabled.set(false)
            }
        }
    }
}

dependencies {

    api(project(Projs.`kbdd`.asDependency))
    api(Libs.log4j_kotlin)

    implementation(Libs.wiremock)
    implementation(Libs.kotlin_jdk8)
    implementation(Libs.kotlin_stdlib)
    implementation(Libs.kotlin_reflect)
    implementation(Libs.kotlinx_coroutines_core)

    testApi(Libs.kotlin_test)

    testImplementation(Libs.rest_assured)
    testImplementation(Libs.jackson_kotlin)
    testImplementation(Libs.jackson_databind)

    testImplementation(Libs.jfix_stdlib_socket)

    testImplementation(Libs.log4j_core)
    testImplementation(Libs.slf4j_over_log4j)

    testImplementation(Libs.junit_api)
    testRuntimeOnly(Libs.junit_engine)

    testImplementation(Libs.groovy)
    testImplementation(Libs.groovy_xml)



    testImplementation(Libs.jfix_corounit_allure)
    testImplementation(Libs.jfix_corounit_engine)
    testImplementation(Libs.koin)
}


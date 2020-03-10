plugins {
    java
    kotlin("jvm")
}


dependencies {

    api(Libs.jfix_corounit_engine)
    api(Libs.jfix_corounit_allure)

    api(Libs.mu_kotlin_logging)
    api(Libs.rest_assured)

    api(Libs.kotlin_test)
    api(Libs.jfix_stdlib_socket)

    api(Libs.jackson_kotlin)
    api(Libs.jackson_databind)


    api(Libs.junit_api)
    api(Libs.junit_engine)
    api(Libs.rest_assured_kotlin)
    api(Libs.groovy)
    api(Libs.groovy_xml)

    api(Libs.kotlin_jdk8)
    api(Libs.kotlin_stdlib)
    api(Libs.kotlin_reflect)

    implementation(Libs.log4j_core)
    implementation(Libs.slf4j_over_log4j)

    testImplementation(Libs.wiremock)


}



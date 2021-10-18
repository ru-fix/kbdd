object Vers {
    //Plugins
    const val gradle_release_plugin = "1.4.3"
    const val dokkav = "1.5.31"
    const val asciidoctor = "1.5.9.2"
    const val allure_plugin = "2.9.4"
    const val allure_cli = "2.15.0"

    //Dependencies
    const val kotlin = "1.5.31"
    const val gradle_kotlin = "1.5.21"
    const val kotlin_coroutines = "1.5.2"

    const val junit = "5.6.0"
    const val rest_assured = "4.4.0"
    const val log4j = "2.13.1"
    const val jackson = "2.10.2"
    const val dynamic_property = "1.1.9"
    const val jfix_stdlib = "2.0.2"
    const val corounit = "1.1.1"
    const val koin = "2.0.1"

    const val aspectj = "1.9.5"
    const val freefair_aspectj = "5.0.1"
}

object Libs {
    //Plugins
    val gradle_release_plugin = "ru.fix:gradle-release-plugin:${Vers.gradle_release_plugin}"
    val dokka_gradle_plugin = "org.jetbrains.dokka:dokka-gradle-plugin:${Vers.dokkav}"
    val nexus_staging_plugin = "io.codearte.nexus-staging"
    val nexus_publish_plugin = "de.marcphilipp.nexus-publish"
    val asciidoctor = "org.asciidoctor:asciidoctor-gradle-plugin:${Vers.asciidoctor}"

    //Dependencies
    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Vers.kotlin}"
    const val kotlin_jdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Vers.kotlin}"
    const val kotlin_reflect = "org.jetbrains.kotlin:kotlin-reflect:${Vers.kotlin}"

    const val gradle_kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Vers.gradle_kotlin}"
    const val gradle_kotlin_jdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Vers.gradle_kotlin}"

    const val kotlinx_coroutines_core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Vers.kotlin_coroutines}"

    const val groovy = "org.codehaus.groovy:groovy:3.0.1"
    const val groovy_xml = "org.codehaus.groovy:groovy-xml:3.0.1"

    const val dynamic_property_api = "ru.fix:dynamic-property-api:${Vers.dynamic_property}"

    const val dynamic_property_std_source = "ru.fix:dynamic-property-std-source:${Vers.dynamic_property}"
    const val dynamic_property_jackson = "ru.fix:dynamic-property-jackson:${Vers.dynamic_property}"
    const val dynamic_property_spring = "ru.fix:dynamic-property-spring:${Vers.dynamic_property}"
    const val jfix_stdlib_files = "ru.fix:jfix-stdlib-files:${Vers.jfix_stdlib}"

    const val jfix_stdlib_concurrency = "ru.fix:jfix-stdlib-concurrency:${Vers.jfix_stdlib}"
    const val jfix_corounit_engine = "ru.fix:corounit-engine:${Vers.corounit}"
    const val jfix_corounit_allure = "ru.fix:corounit-allure:${Vers.corounit}"

    const val log4j_core = "org.apache.logging.log4j:log4j-core:${Vers.log4j}"
    const val slf4j_over_log4j = "org.apache.logging.log4j:log4j-slf4j-impl:${Vers.log4j}"
    const val mu_kotlin_logging = "io.github.microutils:kotlin-logging:1.7.8"
    const val log4j_kotlin = "org.apache.logging.log4j:log4j-api-kotlin:1.0.0"


    const val jackson_dataformat = "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${Vers.jackson}"
    const val jackson_databind = "com.fasterxml.jackson.core:jackson-databind:${Vers.jackson}"
    const val jackson_kotlin = "com.fasterxml.jackson.module:jackson-module-kotlin:${Vers.jackson}"
    const val jackson_jsr310 = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Vers.jackson}"

    //1.9.3 has a bug
    //https://github.com/mockk/mockk/issues/280
    const val mockk = "io.mockk:mockk:1.9.2"

    // Tests
    const val junit_api = "org.junit.jupiter:junit-jupiter-api:${Vers.junit}"
    const val junit_params = "org.junit.jupiter:junit-jupiter-params:${Vers.junit}"
    const val junit_engine = "org.junit.jupiter:junit-jupiter-engine:${Vers.junit}"


    const val rest_assured = "io.rest-assured:rest-assured:${Vers.rest_assured}"
    const val rest_assured_kotlin = "io.rest-assured:kotlin-extensions:${Vers.rest_assured}"

    const val kotlin_test = "io.kotest:kotest-assertions-core-jvm:4.6.3"

    const val wiremock = "com.github.tomakehurst:wiremock:2.26.1"
    const val jfix_stdlib_socket = "ru.fix:jfix-stdlib-socket:${Vers.jfix_stdlib}"

    const val koin = "org.koin:koin-core:${Vers.koin}"
}

enum class Projs {
    `kbdd`,
    `kbdd-example`
    ;

    val asDependency get(): String = ":$name"
}





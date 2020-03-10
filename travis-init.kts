import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Files.newInputStream
import java.nio.file.Paths
import java.util.*

println("Please, login to travis before running this script")
println("travis login --com")

println("Travis version: ${execute("travis", "version")}")
val userHome = System.getProperty("user.home")

val gradlePropertiesFile = Paths.get(userHome, "/.gradle/gradle.properties")
println("Looking for properties in: $gradlePropertiesFile")
val gradleProperties = Properties().apply { newInputStream(gradlePropertiesFile).use { load(it) } }

val secureItems = listOf("repositoryUrl", "repositoryUser", "repositoryPassword", "signingKeyId", "signingPassword")

println("Found:")
for (item in secureItems) {
    val value = gradleProperties[item]
    println("$item=$value")
    requireNotNull(value){
        "Property $item should not be null"
    }
}

println("Encrypt properties")

val secure = ArrayList<String>()
for (item in secureItems) {
    secure.add(execute("travis", "encrypt", "${item}=${gradleProperties[item]}").trim())
}


println("encrypt secring.gpg")
val secringFile = "${userHome}/.gnupg/secring.gpg"
println("Looking for secring.gpg in: ${secringFile}")

if (!Files.exists(Paths.get(secringFile))) {
    println("secring.gpg not found")
    System.exit(1)
}


val secringFileEnc = Paths.get("secring.gpg.enc")
if (Files.exists(secringFileEnc)) {
    println("${secringFileEnc} already exist. Removing it")
    Files.delete(secringFileEnc)
}

val fileEncryptionOutput = execute("travis", "encrypt-file", secringFile)
println("looking for key in output:\n $fileEncryptionOutput")
val key = "encrypted_([^_]+)_key".toRegex().find(fileEncryptionOutput)!!.groupValues[1]

println("#build")
println("before_script: if [[ \$encrypted_${key}_key ]]; then openssl aes-256-cbc -K \$encrypted_${key}_key -iv \$encrypted_${key}_iv -in secring.gpg.enc -out secring.gpg -d; fi")

println("#deploy")
println("before_script: openssl aes-256-cbc -K \$encrypted_${key}_key -iv \$encrypted_${key}_iv -in secring.gpg.enc -out secring.gpg -d")

for (item in secure) {
    println("- secure: $item")
}


fun execute(vararg command: String): String {
    println(command.joinToString(separator = " "))

    val process = ProcessBuilder(*command)
            .start()

    val output = BufferedReader(InputStreamReader(process.inputStream)).useLines { lines ->
        StringBuilder().also { for (line in lines) it.appendln(line) }
    }

    val errors = BufferedReader(InputStreamReader(process.errorStream)).useLines { lines ->
        StringBuilder().also { for (line in lines) it.appendln(line) }
    }
    System.err.println(errors)

    return output.toString()
}

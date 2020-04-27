# KBDD
Kotlin BDD Framework based on Corounit test engine  
[![Maven Central](https://img.shields.io/maven-central/v/ru.fix/kbdd.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22ru.fix%22)

## KBDD Framework
KBDD based on suspendable methods. 
This allows big amount of test suite to start and work simultaneously and dramatically reduce time required for behave tests to complete.

In order to achieve good speed test designer should take into consideration next practices:

* All test and step methods are suspendable, do not block current thread - suspend it. 
* Use dedicated thread pools for blocking operations like network calls.
* If you sent command to the tested system and waiting for results do it actively. Instead of:
```kotlin
MySystem.`Send command to start heavy process`()
delay(15_000)
MySystem.`Get result of process`{
    bodyJson()["status"].isEquals("activated")
}
```
* Use active polling:
```kotlin
MySystem.`Send command to start heavy process`()
repeatUntilSuccess{
    MySystem.`Get result of process`{
        bodyJson()["status"].isEquals("activated")
    }
}
```
## Example
Example can be found at `jfix-kbdd-example` module.

## Rest

### Methods
* `Rest.request` provides DSL for building HTTP request
* `Rest.statusCode()` provide access to response status code
* `Rest.statusLine()` provide access to response status line
* `Rest.bodyJson()` provide access to response body as a json object
* `Rest.bodyString()` provide access to response body as a String

### Json DSL
```kotlin
request {
    baseUri(settings.myServiceBaseUri)
    post("/auth-service/check-permission")
    body {
        "identity" {
            "token" % "03A40BC430D9F3579D8CA"
        }
    }
}

statusCode().isEquals(200)
bodyJson()["error"].isNull()
bodyJson()["permissions"].first{
    it["resource"].isEquals("booking")
}["status"].isEquals("active")
```

### POJO
```kotlin
data class Identity(val token: String)

request {
    baseUri(settings.myServiceBaseUri)
    post("/auth-service/check-permission")
    body (Identity(toke="03A40BC430D9F3579D8CA"))
}

statusCode().isEquals(200)
bodyJson()["error"].isNull()
bodyJson()["permissions"].first{
    it["resource"].isEquals("booking")
}["status"].isEquals("active")
```

### Do not send nulls

```kotlin
 request {
    baseUri(server.baseUrl())
    post("/json-post-with-nulls")
    body {
        "one" % 1
        "two" % null
    }
}
```
```json
{
  "one": 1,
  "two": null
}
```

```kotlin
 request {
    baseUri(server.baseUrl())
    post("/json-post-without-nulls")
    body(sendNulls=false) {
        "one" % 1
        "two" % null
    }
}
```
```json
{
  "one": 1
}
```

## Custom assertions and response navigation
All asserts and navigation methods implemented via extension function to the `Checkable` and `Explorable` interfaces.
To add new one simply write your own extension functions to these interfaces and follow same convention.

Build in assertion function example:

```kotlin
bodyJson()["result"].isLessThanOrEqual(12)
```

```kotlin
fun Checkable.isLessThanOrEqual(other: Any) = express { source ->
    object : Expression {
        override fun print(): String = "${source.print()} <= $other"
        override fun evaluate(): Boolean = (source.evaluate() as Comparable<Any?>) <= other
    }
}

```
Build in navigation function example:
```kotlin
bodyJson()["entries"].single()["data"].isEquals(12)
```
```kotlin
fun Explorable.single() = navigate {
    object : Navigation {
        override fun path() = "$path.single()"
        override fun node(): Any? {
            val node = requireNotNullList(path())
            require(node.size == 1) {
                "Failed to evaluatte $path(). Expected single element in the List. Actual: $node"
            }
            return node[0]
        }
    }
}
```

## Best practice

### Add reference to documentation in @Description 
Scenario description have a reference to project wiki or documentation with detailed description of tested cases.
```kotlin
@Description("""
    User makes a simple purchase in the site 
    http://documentation.acme.com/purchase/details
    """)
class PurchaseTest(){
    //...
}
```
### String steps describe hi level scenario
Use string steps to describe business process in clear way that all members of your team, including non-tech people, easily understand. 
This will lead to a clear readable Allure report.
```kotlin
suspend fun `make a purchase in the shop`(){
    "Ensure that user account with amount of 100 exist"{
        //...    
    }   
    "User adds item of price 45 into the basket"{
        //...
    }
    "User creates a purchase order"{
        //...
    }
    "User select shipment condition"{
        //...
    }       
    "User agrees for money withdraw from use account"{
        //...
    }
    "User account balance became 55"{
        //...
    }   
}
```
### Each test prepare it's own data in self-recoverable way
Keep in mind that all tests are running in parallel. 
Our task is to make tests independent on each other. 
Best way to do that is through tested system configuration. 
E.g. we can use unique account id for each test case.  
Since test can broke on eny step we should take into consideration that should be able
to restart the test.     
So our test should be able to reset test conditions and system state that was corrupted due to
previous failed test run. 

```kotlin
suspend fun `make a purchase in the shop`(){
    val userAccount = 9473234983L
    "Ensure that user account with amount of 100 exist"{
        //create account 9473234983L with amount 100 if such account does not exist yet
        //if account exist, then set account amount to 100    
        //...
    }   
    "User adds item of price 45 into the basket"{
        //...
    }
    //...
}
``` 

## Real time Allure report update

Gradle Allure Plugin allows to start a daemon, that will monitor allure-results. 
Daemon will rebuild Allure html report after each test run.  
This option is handy when you are writing new test and want to immediately see updates of the Allure report in real time. 
```shell script
gradle allureReport -t
```

## How to integrate in to your project

It is often useful to run kbb tests as separate step in your build pipeline. 
This allows not to run integraton kbb scenarios and unit test within same gradle test task. 
```text
my-project
 ┕ my-a-module
 ┕ my-b-module
 ┕ my-c-module
 ┕ kbdd-test
   ┕ build.kts
```
Disable `test` task in kbdd-test module and renaming it to `kbdd`
 ```kotlin
tasks.test {
    onlyIf { false }
}

tasks.register("kbdd", org.gradle.api.tasks.testing.Test::class) {
    dependsOn(":${Projs.`crudility-server`.name}:crudility-server-during-build")
    outputs.upToDateWhen { false }
}
```
Now you can use
* `gradle build` to build and test your project as usual without involving kbb-test module
* `gradle kbb` to start kbb tests in your pipeline


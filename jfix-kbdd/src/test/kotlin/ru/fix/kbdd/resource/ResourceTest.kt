package ru.fix.kbdd.resource

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class MyResource{
    lateinit var host: String
}

class ResourceTest{

    @Test
    fun `interpolate placeholder in string by variables from system property`(){
        System.setProperty("my-system-property", "system-value")

        val myResource = Resource.loadJsonWithPlaceholders<MyResource>("resource-with-placeholders.json")

        myResource.host.shouldBe("http://system-value/path")
    }

}
package ru.fix.kbdd.resource

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class MyResource{
    lateinit var lateinitValue: String
    var withDefaultValue = "default"
    var withOverriddenDefaultValue = "default"
    var withMissingPlaceholder = ""
    var compositeWithMissingPlaceholder = ""
}

class ResourceTest{

    @Test
    fun `interpolate placeholder in string by variables from system property`(){
        System.setProperty("my-system-property", "system-value")

        val myResource = Resource.loadJsonWithPlaceholders<MyResource>("resource-with-placeholders.json")

        myResource.lateinitValue.shouldBe("http://system-value/path")
        myResource.withDefaultValue.shouldBe("default")
        myResource.withOverriddenDefaultValue.shouldBe("overridden")
        myResource.withMissingPlaceholder.shouldBe("\${missing-placeholder}")
        myResource.compositeWithMissingPlaceholder.shouldBe("com-\${missing-placeholder}-posite")
    }

}
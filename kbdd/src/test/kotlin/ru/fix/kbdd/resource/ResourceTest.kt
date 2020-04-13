package ru.fix.kbdd.resource

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class MyResource{
    lateinit var lateinitValue: String
    var withDefaultValue = "default"
    var withOverriddenDefaultValue = "default"
    var withMissingPlaceholder = ""
    var compositeWithMissingPlaceholder = ""

    class Complex{
        var name = "name"
        var value = "value"
    }
    var defaultComplex = Complex()
    var complexWithOverriddenName = Complex()
    var overridenComplex = Complex()
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

        myResource.defaultComplex.name.shouldBe("name")
        myResource.defaultComplex.value.shouldBe("value")

        myResource.complexWithOverriddenName.name.shouldBe("overriddenName")
        myResource.complexWithOverriddenName.value.shouldBe("value")

        myResource.overridenComplex.name.shouldBe("name2")
        myResource.overridenComplex.value.shouldBe("value2")

    }

}
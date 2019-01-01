package util

import es.iaaa.kubby.util.toTitleCase
import org.junit.Test
import kotlin.test.assertEquals

class ExtensionsTest {

    @Test
    fun `toTitleCase converts an English string to title case`() {
        assertEquals("Berlin is a City in Germany", "Berlin is a city in Germany".toTitleCase("en"))
    }

    @Test
    fun `toTitleCase converts use English as default language`() {
        assertEquals("Berlin is a City in Germany", "Berlin is a city in Germany".toTitleCase())
    }

    @Test
    fun `toTitleCase trims and collapses consecutive whitespace`() {
        assertEquals("Berlin is a City in Germany", " Berlin  is  a  city  in  Germany ".toTitleCase("en"))
    }

    @Test
    fun `toTitleCase splits camel case`() {
        assertEquals("Berlin is a City in Germany", " Berlin is a CityInGermany ".toTitleCase("en"))
    }
}
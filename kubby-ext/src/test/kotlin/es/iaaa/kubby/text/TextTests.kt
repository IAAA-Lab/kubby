package es.iaaa.kubby.text

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TextTests {

    lateinit var excludeWords: List<String>

    @BeforeTest
    fun before() {
        excludeWords = listOf("is", "a", "in")
    }

    @Test
    fun `toTitleCase converts an English string to title case`() {
        assertEquals("Berlin is a City in Germany", "Berlin is a city in Germany".toTitleCase(excludeWords))
    }

    @Test
    fun `toTitleCase trims and collapses consecutive whitespace`() {
        assertEquals(
            "Berlin is a City in Germany",
            " Berlin  is  a  city  in  Germany ".toTitleCase(excludeWords)
        )
    }

    @Test
    fun `toTitleCase splits camel case`() {
        assertEquals("Berlin is a City in Germany", " Berlin is a CityInGermany ".toTitleCase(excludeWords))
    }
}
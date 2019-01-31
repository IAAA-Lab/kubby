package es.iaaa.kubby.text

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class TextTests {

    lateinit var excludeWords: List<String>

    @Before
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
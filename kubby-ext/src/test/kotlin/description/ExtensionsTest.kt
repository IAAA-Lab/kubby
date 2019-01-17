package es.iaaa.kubby.description

import io.ktor.config.ApplicationConfig
import io.ktor.server.engine.commandLineEnvironment
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ExtensionsTest {

    lateinit var runtimeConfig: ApplicationConfig

    @Before fun before() {
        runtimeConfig = commandLineEnvironment(arrayOf("-port=80")).config
    }

    @Test fun `toTitleCase converts an English string to title case`() {
        assertEquals("Berlin is a City in Germany", "Berlin is a city in Germany".toTitleCase("en", runtimeConfig))
    }

    @Test fun `toTitleCase converts use English as default language`() {
        assertEquals("Berlin is a City in Germany", "Berlin is a city in Germany".toTitleCase(null, runtimeConfig))
    }

    @Test fun `toTitleCase trims and collapses consecutive whitespace`() {
        assertEquals(
            "Berlin is a City in Germany",
            " Berlin  is  a  city  in  Germany ".toTitleCase("en", runtimeConfig)
        )
    }

    @Test fun `toTitleCase splits camel case`() {
        assertEquals("Berlin is a City in Germany", " Berlin is a CityInGermany ".toTitleCase("en", runtimeConfig))
    }
}
package es.iaaa.kubby.config

import com.typesafe.config.Config
import es.iaaa.kubby.commandLineConfig
import es.iaaa.kubby.rest.api.Routes
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RoutesConfigDefaultTest {

    lateinit var config: Config

    @BeforeTest
    fun before() {
        config = commandLineConfig(arrayOf())
    }

    @Test
    fun `ensure the default routes are defined`() {
        val expected = Routes(
            "/page",
            "/data",
            "/resource"
        )
        assertEquals(expected, config.toRoutes())
    }
}

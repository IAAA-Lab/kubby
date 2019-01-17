package es.iaaa.kubby.description

import com.typesafe.config.ConfigFactory
import es.iaaa.kubby.fixtures.Models
import io.ktor.config.HoconApplicationConfig
import org.junit.Before
import org.junit.Test
import org.koin.test.AutoCloseKoinTest
import kotlin.test.assertEquals

class DescriptionHandlerTest  : AutoCloseKoinTest() {

    private lateinit var node: ContentNode

    @Before fun before() {
        val config = HoconApplicationConfig(ConfigFactory.load("test.conf"))
        val model = Models.aSimpleModel("http://example.com/sample")
        node = DescriptionHandler(config).contentOf(model.getResource("http://example.com/sample"), "http://example.com/data/sample")
    }

    @Test fun `ensure project name is defined`()  {
        assertEquals("Test project", node["projectName"])
    }

    @Test fun `ensure title is defined`()  {
        assertEquals("Jane Doe", node["title"])
    }

    @Test fun `ensure home page is defined`()  {
        assertEquals("http://example.com", node["projectHomepage"])
    }

    @Test fun `ensure RDF link is defined`()  {
        assertEquals("http://example.com/data/sample", node["rdfLink"])
    }

    @Test fun `ensure RDF format is defined`()  {
        assertEquals("application/ld+json", node["rdfFormat"])
    }
}
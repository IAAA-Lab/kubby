package es.iaaa.kubby.app.pubby.rest.api

import com.typesafe.config.ConfigFactory
import es.iaaa.kubby.fixtures.Models
import io.ktor.config.HoconApplicationConfig
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class EntityDtoTest {

    private lateinit var node: Map<String,Any>

    @Before fun before() {
        val config = HoconApplicationConfig(ConfigFactory.load("test.conf"))
        val model = Models.aSimpleModel("http://example.com/sample")
        node = model.getResource("http://example.com/sample")
            .toEntityDto(config, "http://example.com/data/sample")
            .toMap()
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
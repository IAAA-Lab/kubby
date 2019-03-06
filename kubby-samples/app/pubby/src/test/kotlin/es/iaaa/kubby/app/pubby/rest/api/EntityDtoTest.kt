package es.iaaa.kubby.app.pubby.rest.api

import com.typesafe.config.ConfigFactory
import es.iaaa.kubby.app.pubby.fixtures.Models.aSimpleModel
import es.iaaa.kubby.app.pubby.toEntityDto
import es.iaaa.kubby.app.pubby.toMap
import es.iaaa.kubby.config.toProjectDescription
import es.iaaa.kubby.domain.impl.ResourceEntityImpl
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class EntityDtoTest {

    private lateinit var node: Map<String, Any>

    @Before
    fun before() {
        val config = ConfigFactory.load("test.conf").toProjectDescription()
        val model = aSimpleModel("http://example.com/sample")
        node = ResourceEntityImpl(uri = "http://example.com/sample", model = model)
            .toEntityDto(config, "http://example.com/dataUri/sample")
            .toMap()
    }

    @Test
    fun `ensure project name is defined`() {
        assertEquals("Test project", node["projectName"])
    }

    @Test
    fun `ensure title is defined`() {
        assertEquals("Jane Doe", node["title"])
    }

    @Test
    fun `ensure home page is defined`() {
        assertEquals("http://example.com", node["projectHomepage"])
    }

    @Test
    fun `ensure RDF link is defined`() {
        assertEquals("http://example.com/dataUri/sample", node["rdfLink"])
    }

    @Test
    fun `ensure RDF format is defined`() {
        assertEquals("application/ld+toJson", node["rdfFormat"])
    }
}
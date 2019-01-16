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

    @Before
    fun before() {
        val config = HoconApplicationConfig(ConfigFactory.load("test.conf"))
        val model = Models.aSimpleModel("http://example.com/sample")
        node = DescriptionHandler(config).contentOf(model.getResource("http://example.com/sample"), "http://example.com/data/sample")
    }

    @Test
    fun testProjectName()  {
        assertEquals("Test project", node["projectName"])
    }

    @Test
    fun testTitle()  {
        assertEquals("Jane Doe", node["title"])
    }

    @Test
    fun testProjectHomepage()  {
        assertEquals("http://example.com", node["projectHomepage"])
    }

    @Test
    fun testRdfLink()  {
        assertEquals("http://example.com/data/sample", node["rdfLink"])
    }


    @Test
    fun testRdfFormat()  {
        assertEquals("application/ld+json", node["rdfFormat"])
    }
}
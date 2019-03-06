package es.iaaa.kubby.config

import com.typesafe.config.Config
import es.iaaa.kubby.commandLineConfig
import es.iaaa.kubby.fixtures.defaultUsePrefixes
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectDescriptionConfigTest {

    lateinit var config: Config

    @BeforeTest
    fun before() {
        val file = ProjectDescriptionConfigTest::class.java.getResource("/config/test.conf").file
        config = commandLineConfig(arrayOf("-config=$file"))
    }

    @Test
    fun `project name can be configured`() {
        assertEquals("Test DBpedia.org", config.toProjectDescription().projectName)
    }

    @Test
    fun `project homepage can be configured`() {
        assertEquals("http://dbpedia.org", config.toProjectDescription().projectHomepage)
    }

    @Test
    fun `label properties can be configured`() {
        val expected = listOf("http://example.com/ns#label")
        assertEquals(expected, config.toProjectDescription().labelProperties.map { it.uri })
    }

    @Test
    fun `comment properties can be configured`() {
        val expected = listOf("http://example.com/ns#comment")
        assertEquals(expected, config.toProjectDescription().commentProperties.map { it.uri })
    }

    @Test
    fun `image properties can be configured`() {
        val expected = listOf("http://example.com/ns#image")
        assertEquals(expected, config.toProjectDescription().imageProperties.map { it.uri })
    }


    @Test
    fun `use prefixes can be expanded`() {
        assertEquals("http://example.com/ns#", config.toProjectDescription().usePrefixes["ex"])
    }

    @Test
    fun `default prefixes are available`() {
        defaultUsePrefixes.forEach { prefix, uri ->
            assertEquals(uri, config.toProjectDescription().usePrefixes[prefix])
        }
    }

    @Test
    fun `default language can be configured`() {
        assertEquals("es", config.toProjectDescription().defaultLanguage)
    }


    @Test
    fun `index resource can be configured`() {
        assertEquals("http://dbpedia.org/resource/DBpedia", config.toProjectDescription().indexResource)
    }
}



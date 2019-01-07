package es.iaaa.kubby.config

import com.typesafe.config.ConfigFactory
import io.ktor.config.ApplicationConfig
import io.ktor.config.HoconApplicationConfig
import io.ktor.server.engine.commandLineEnvironment
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class ApplicationConfigExtTest {

    lateinit var config: ApplicationConfig

    lateinit var runtimeConfig: ApplicationConfig

    @Before
    fun before() {
        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource("config/test.conf").file)
        config = HoconApplicationConfig(ConfigFactory.parseFile(file))
        runtimeConfig = commandLineEnvironment(arrayOf("-port=80")).config

    }

    @Test
    fun `test access to the project name`() {
        assertEquals("Test DBpedia.org", config.projectName)
    }

    @Test
    fun `test access to the project homepage`() {
        assertEquals("http://dbpedia.org", config.projectHomepage)
    }

    @Test
    fun `test access to the prefixes of the project`() {
        val expected = mapOf(
            "rdf" to "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            "dc" to "http://purl.org/dc/elements/1.1/",
            "dcterms" to "http://purl.org/dc/terms/",
            "foaf" to "http://xmlns.com/foaf/0.1/",
            "schema" to "http://schema.org/"
        )
        assertEquals(expected, config.usePrefixes)
    }

    @Test
    fun `test access to the default prefixes of the project`() {
        val expected = mapOf(
            "rdfs" to "http://www.w3.org/2000/01/rdf-schema#",
            "rdf" to "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            "dc" to "http://purl.org/dc/elements/1.1/",
            "dcterms" to "http://purl.org/dc/terms/",
            "foaf" to "http://xmlns.com/foaf/0.1/",
            "schema" to "http://schema.org/"
        )
        assertEquals(expected, runtimeConfig.usePrefixes)
    }

    @Test
    fun `test access to the default language`() {
        assertEquals("en", config.defaultLanguage)
    }

    @Test
    fun `test access to the index resource`() {
        assertEquals("http://dbpedia.org/resource/DBpedia", config.indexResource)
    }

    @Test
    fun `test access to the configured datasets`() {
        val expect = SparqlEndpoint(
            endpoint = "https://dbpedia.org/sparql",
            trustEndpoint = true,
            defaultGraph = "http://dbpedia.org",
            datasetBase = "http://dbpedia.org/resource/"
        )
        assertEquals(1, config.datasets.size)
        assertEquals(expect, config.datasets[0])
    }

    @Test
    fun `test access to the default label properties of the project`() {
        val expected = listOf(
            "http://www.w3.org/2000/01/rdf-schema#label",
            "http://purl.org/dc/elements/1.1/title",
            "http://purl.org/dc/terms/title",
            "http://xmlns.com/foaf/0.1/name",
            "http://schema.org/name"
        )
        assertEquals(expected, runtimeConfig.labelProperties.map { it.uri })
    }
}



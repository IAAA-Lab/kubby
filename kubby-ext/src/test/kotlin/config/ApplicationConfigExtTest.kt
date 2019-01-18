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

    @Before fun before() {
        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource("config/test.conf").file)
        config = HoconApplicationConfig(ConfigFactory.parseFile(file))
        runtimeConfig = commandLineEnvironment(arrayOf("-port=80")).config
    }

    @Test fun `ensure project name has value`() {
        assertEquals("Test DBpedia.org", config.projectName)
    }

    @Test fun `ensure the project homepage has value`() {
        assertEquals("http://dbpedia.org", config.projectHomepage)
    }

    @Test fun `ensure the prefixes are defined`() {
        val expected = mapOf(
            "rdf" to "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            "dc" to "http://purl.org/dc/elements/1.1/",
            "dcterms" to "http://purl.org/dc/terms/",
            "foaf" to "http://xmlns.com/foaf/0.1/",
            "schema" to "http://schema.org/"
        )
        assertEquals(expected, config.usePrefixes)
    }

    @Test fun `ensure the default prefixes are defined`() {
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

    @Test fun `ensure the default language is defined`() {
        assertEquals("en", config.defaultLanguage)
    }

    @Test fun `ensure the index resource is defined`() {
        assertEquals("http://dbpedia.org/resource/DBpedia", config.indexResource)
    }

    @Test fun `configure a SPARQL endpoint`() {
        val expect = SPARQLEndpoint(
            endpoint = "https://dbpedia.org/sparql",
            trustEndpoint = true,
            defaultGraph = "http://dbpedia.org",
            datasetBase = "http://dbpedia.org/resource/"
        )
        assertEquals(1, config.datasets.size)
        assertEquals(expect, config.datasets[0])
    }

    @Test fun `ensure to the default label properties are defined`() {
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



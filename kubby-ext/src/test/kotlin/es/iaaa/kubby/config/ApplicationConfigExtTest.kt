package es.iaaa.kubby.config

import com.typesafe.config.ConfigFactory
import es.iaaa.kubby.repository.SparqlEndpoint
import io.ktor.config.HoconApplicationConfig
import io.ktor.server.engine.commandLineEnvironment
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationConfigExtTest {

    lateinit var config: ProjectDescription

    lateinit var runtimeConfig: ProjectDescription

    @BeforeTest
    fun before() {
        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource("config/test.conf").file)
        config = HoconApplicationConfig(ConfigFactory.parseFile(file)).toProjectDescription()
        runtimeConfig = commandLineEnvironment(arrayOf("-port=80")).config.toProjectDescription()
    }

    @Test
    fun `ensure project name has value`() {
        assertEquals("Test DBpedia.org", config.projectName)
    }

    @Test
    fun `ensure the project homepage has value`() {
        assertEquals("http://dbpedia.org", config.projectHomepage)
    }

    @Test
    fun `ensure the prefixes are defined`() {
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
    fun `ensure the default prefixes are defined`() {
        val expected = mapOf(
            "xs" to "http://www.w3.org/2001/XMLSchema#",
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
    fun `ensure the default language is defined`() {
        assertEquals("en", config.defaultLanguage)
    }

    @Test
    fun `ensure the index resource is defined`() {
        assertEquals("http://dbpedia.org/resource/DBpedia", config.indexResource)
    }

    @Test
    fun `configure a SPARQL endpoint`() {
        val expect = SparqlEndpoint(
            service = "https://dbpedia.org/sparql",
            forceTrust = true,
            dataset = "http://dbpedia.org",
            namespace = "http://dbpedia.org/resource/",
            addSameAs = true
        )
        println(expect)
        println(config.datasets[0])
        assertEquals(1, config.datasets.size)
        assertEquals(expect, config.datasets[0])
    }

    @Test
    fun `ensure to the default label properties are defined`() {
        val expected = listOf(
            "http://www.w3.org/2000/01/rdf-schema#label",
            "http://purl.org/dc/elements/1.1/title",
            "http://purl.org/dc/terms/title",
            "http://xmlns.com/foaf/0.1/projectName",
            "http://schema.org/projectName"
        )
        assertEquals(expected, runtimeConfig.labelProperties.map { it.uri })
    }
}



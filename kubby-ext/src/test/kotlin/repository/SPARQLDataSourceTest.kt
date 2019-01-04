package es.iaaa.kubby.repository

import org.apache.jena.sparql.vocabulary.FOAF
import org.junit.Test
import kotlin.test.assertTrue

class SPARQLDataSourceTest {

    @Test
    fun `retrieve the description of a resource from a remote endpoint with issues with its SSL certificate`() {
        val ds = SPARQLDataSource("https://dbpedia.org/sparql", "http://dbpedia.org", true)
        val model = ds.describe("http://dbpedia.org/resource/", "Aragón")
        assertTrue(
            model.contains(
                model.createResource("http://en.wikipedia.org/wiki/Aragón"),
                FOAF.primaryTopic,
                model.createResource("http://dbpedia.org/resource/Aragón")
            )
        )
    }

    @Test
    fun `retrieve the description of a resource from a remote endpoint`() {
        val ds = SPARQLDataSource("http://datos.bne.es/sparql")
        val model = ds.describe("http://datos.bne.es/resource/", "XX85148")
        assertTrue(
            model.contains(
                model.createResource("http://datos.bne.es/resource/bimo0000239213"),
                model.createProperty("http://datos.bne.es/def/OP3004"),
                model.createResource("http://datos.bne.es/resource/XX85148")
            )
        )
    }

}
package es.iaaa.kubby.repository.source

import es.iaaa.kubby.repository.EntityId
import org.apache.jena.sparql.vocabulary.FOAF
import kotlin.test.Test
import kotlin.test.assertTrue

class SPARQLDataSourceTest {

    @Test
    fun `retrieve the description of a resource from a remote endpoint with issues with its SSL certificate`() {
        val ds = SparqlEntityRepository(
            "https://dbpedia.org/sparql",
            "http://dbpedia.org",
            true
        )
        val model = ds.findOne(EntityId("http://dbpedia.org/resource/", "Aragón")).model
        model.apply {
            assertTrue(
                contains(
                    createResource("http://en.wikipedia.org/wiki/Aragón"),
                    FOAF.primaryTopic,
                    createResource("http://dbpedia.org/resource/Aragón")
                )
            )
        }
    }

    @Test
    fun `retrieve the description of a resource from a remote endpoint`() {
        val ds = SparqlEntityRepository("http://datos.bne.es/sparql")
        val model = ds.findOne(EntityId("http://datos.bne.es/resource/", "XX85148")).model
        model.apply {
            assertTrue(
                contains(
                    createResource("http://datos.bne.es/resource/bimo0000239213"),
                    createProperty("http://datos.bne.es/def/OP3004"),
                    createResource("http://datos.bne.es/resource/XX85148")
                )
            )
        }
    }

}
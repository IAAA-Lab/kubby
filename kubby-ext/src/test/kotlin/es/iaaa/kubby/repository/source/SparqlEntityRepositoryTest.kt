package es.iaaa.kubby.repository.source

import org.apache.jena.rdf.model.Model
import org.apache.jena.sparql.vocabulary.FOAF
import kotlin.test.Test
import kotlin.test.assertTrue

class SparqlEntityRepositoryTest {

    @Test
    fun `retrieve the description of a resource from a remote endpoint with issues with its SSL certificate`() {
        val ds = SparqlEntityRepository(
            "https://dbpedia.org/sparql",
            "http://dbpedia.org",
            true
        )
        val model = ds.findOne("http://dbpedia.org/resource/", "Aragón").toGraphModel() as Model
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
        val model = ds.findOne("http://datos.bne.es/resource/", "XX85148").toGraphModel() as Model
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
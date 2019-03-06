package es.iaaa.kubby.config

import com.typesafe.config.ConfigFactory
import es.iaaa.kubby.repository.source.*
import java.nio.file.Paths
import kotlin.test.*

class EntityRepositoryConfigTest {

    val sparqlConfig = ConfigFactory.parseMap(
        mapOf(
            "sparqlEndpoint" to "https://dbpedia.org/sparql",
            "sparqlDefaultGraph" to "http://dbpedia.org",
            "datasetBase" to "https://dbpedia.org/",
            "addSameAsStatements" to "true",
            "trustEndpoint" to "true",
            "attribution" to "mine"
        )
    )

    val tdb2Config = ConfigFactory.parseMap(
        mapOf(
            "datasetBase" to "https://dbpedia.org/",
            "addSameAsStatements" to "true",
            "type" to "tdb2",
            "path" to "dbpedia",
            "mode" to "create",
            "data" to "dbpedia.ttl",
            "attribution" to "mine"
        )
    )

    @Test
    fun `map configuration to repositories`() {
        val list = listOf(sparqlConfig, tdb2Config).toEntityRepositories()
        assertEquals(2, list.size)
        val sparql = list[0]
        if (sparql is RewrittenEntityRepository) {
            assertEquals("https://dbpedia.org/", sparql.prefix)
            assertEquals(true, sparql.addSameAs)
            assertTrue(sparql.repository is SparqlEntityRepository)
        } else {
            fail()
        }
        val tdb2 = list[1]
        if (tdb2 is RewrittenEntityRepository) {
            assertEquals("https://dbpedia.org/", tdb2.prefix)
            assertEquals(true, tdb2.addSameAs)
            assertTrue(tdb2.repository is Tdb2EntityRepository)
        } else {
            fail()
        }
    }


    @Test
    fun `build SPARQL backed repository`() {
        sparqlConfig.toSparqlEntityRepository().apply {
            assertEquals("https://dbpedia.org/sparql", service)
            assertEquals("http://dbpedia.org", dataset)
            assertEquals("mine", attribution)
            assertTrue(forceTrust)
        }
    }

    @Test
    fun `build TDB2 backed repository`() {
        tdb2Config.toTDB2EntityRepository().apply {
            assertEquals(Paths.get("dbpedia"), path)
            assertEquals(RepositoryMode.CREATE, mode)
            assertEquals("mine", attribution)
            assertEquals(Paths.get("dbpedia.ttl"), data)
        }
    }

    @AfterTest
    fun afterTest() {
        val path = Paths.get("dbpedia")
        path.deleteRecursivelyIfExists()
    }

}

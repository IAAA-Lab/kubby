package es.iaaa.kubby.config

import es.iaaa.kubby.repository.source.RepositoryMode
import es.iaaa.kubby.repository.source.deleteRecursivelyIfExists
import io.ktor.config.ApplicationConfig
import io.ktor.config.MapApplicationConfig
import java.nio.file.Paths
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EntityRepositoryConfigTest {

    @Test
    fun `build SPARQL backed repository`() {
        val config: ApplicationConfig = MapApplicationConfig(
            "endpoint" to "https://dbpedia.org/sparql",
            "default-graph" to "http://dbpedia.org",
            "trust-endpoint" to "true"
        )
        config.toSparqlEntityRepository().apply {
            assertEquals("https://dbpedia.org/sparql", service)
            assertEquals("http://dbpedia.org", dataset)
            assertTrue(forceTrust)
        }
    }

    @Test
    fun `build TDB2 backed repository`() {
        val config: ApplicationConfig = MapApplicationConfig(
            "path" to "dbpedia",
            "mode" to "create",
            "dataUri" to "dbpedia.ttl"
        )
        config.toTDB2EntityRepository().apply {
            assertEquals(Paths.get("dbpedia"), path)
            assertEquals(RepositoryMode.CREATE, mode)
            assertEquals(Paths.get("dbpedia.ttl"), data)
        }
    }

    @AfterTest
    fun afterTest() {
        val path = Paths.get("dbpedia")
        path.deleteRecursivelyIfExists()
    }

}

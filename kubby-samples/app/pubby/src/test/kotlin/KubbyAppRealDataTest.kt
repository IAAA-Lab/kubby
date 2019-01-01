package es.iaaa.kubby

import es.iaaa.kubby.config.Configuration
import es.iaaa.kubby.config.module
import es.iaaa.kubby.repository.DataSource
import es.iaaa.kubby.server.main
import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.RDFS
import org.junit.Before
import org.koin.standalone.StandAloneContext
import org.koin.standalone.inject
import org.koin.test.AutoCloseKoinTest
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KubbyAppRealDataTest : AutoCloseKoinTest() {

    private val dao: DataSource by inject()

    @Before
    fun before() {
        StandAloneContext.startKoin(listOf(module))
    }

    @Test
    fun testData() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Get, "${Configuration.route.data}/Tetris")) {
            assertEquals(HttpStatusCode.OK, response.status())
            val model = ModelFactory.createDefaultModel()
            RDFDataMgr.read(model, StringReader(response.content), null, Lang.JSONLD)
            assertTrue(
                model.contains(
                    model.createResource("http://localhost/resource/Tetris"),
                    RDFS.seeAlso, model.createResource("http://localhost/resource/Korobeiniki")
                )
            )
            assertTrue(
                model.contains(
                    model.createResource("http://localhost/data/Tetris"),
                    FOAF.primaryTopic, model.createResource("http://localhost/resource/Tetris")
                )
            )
        }
    }
}


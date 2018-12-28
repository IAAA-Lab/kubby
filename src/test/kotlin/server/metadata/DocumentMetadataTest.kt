package es.iaaa.kubby.server.metadata

import es.iaaa.kubby.config.Configuration
import es.iaaa.kubby.datasource.DataSource
import es.iaaa.kubby.fixtures.Models.aSimpleModel
import es.iaaa.kubby.server.main
import es.iaaa.kubby.server.module
import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.sparql.vocabulary.FOAF
import org.junit.Before
import org.junit.Test
import org.koin.standalone.StandAloneContext
import org.koin.standalone.inject
import org.koin.test.AutoCloseKoinTest
import org.koin.test.declareMock
import org.mockito.BDDMockito.given
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KubbyAppTest : AutoCloseKoinTest() {

    private val dao: DataSource by inject()

    @Before
    fun before() {
        StandAloneContext.startKoin(listOf(module))
        declareMock<DataSource>()
    }

    @Test
    fun testData() = withTestApplication(Application::main) {
        given(dao.describe("http://localhost/resource/", "1")).will { aSimpleModel("http://localhost/resource/1") }
        with(handleRequest(HttpMethod.Get, "${Configuration.route.data}/1") ) {
            assertEquals(HttpStatusCode.OK, response.status())
            val model = ModelFactory.createDefaultModel()
            RDFDataMgr.read(model, StringReader(response.content), null, Lang.JSONLD)
            assertTrue(model.contains(model.createResource("http://localhost/data/1"),
                FOAF.primaryTopic, model.createResource("http://localhost/resource/1")))
        }
    }
}
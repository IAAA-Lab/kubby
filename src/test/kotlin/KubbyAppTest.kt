package es.iaaa.kubby

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
import org.junit.Before
import org.koin.standalone.StandAloneContext
import org.koin.standalone.inject
import org.koin.test.AutoCloseKoinTest
import org.koin.test.declareMock
import org.mockito.BDDMockito.given
import kotlin.test.Test
import kotlin.test.assertEquals

class KubbyAppTest : AutoCloseKoinTest() {

    private val dao: DataSource by inject()

    @Before
    fun before() {
        StandAloneContext.startKoin(listOf(module))
        declareMock<DataSource>()
    }

    @Test
    fun testIndex() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Get, "/")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("index", response.content)
        }
    }

    @Test
    fun testResource() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Get, "${Configuration.route.resource}/1")) {
            assertEquals(HttpStatusCode.Found, response.status())
            assertEquals("${Configuration.route.data}/1", response.headers["Location"])
        }
    }

    @Test
    fun testData() = withTestApplication(Application::main) {
        given(dao.describe("http://localhost/resource/", "1")).will { aSimpleModel("http://localhost/resource/1") }
        with(handleRequest(HttpMethod.Get, "${Configuration.route.data}/1") ) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(
                """
                |{
                |  "@graph" : [ {
                |    "@id" : "http://localhost/data/1",
                |    "foaf:primaryTopic" : {
                |      "@id" : "http://localhost/resource/1"
                |    }
                |  }, {
                |    "@id" : "http://localhost/resource/1",
                |    "@type" : "schema:Person",
                |    "schema:jobTitle" : "Professor",
                |    "schema:name" : "Jane Doe",
                |    "schema:url" : "http://www.janedoe.com",
                |    "rdfs:seeAlso" : {
                |      "@id" : "http://www.ex.com/janedoe/moreinfo"
                |    }
                |  } ],
                |  "@context" : {
                |    "foaf" : "http://xmlns.com/foaf/0.1/",
                |    "rdfs" : "http://www.w3.org/2000/01/rdf-schema#",
                |    "schema" : "http://schema.org/"
                |  }
                |}
                |
            """.trimMargin(), response.content
            )
        }
    }


    @Test
    fun testPage() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Get, "${Configuration.route.page}/1")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("page", response.content)
        }
    }
}


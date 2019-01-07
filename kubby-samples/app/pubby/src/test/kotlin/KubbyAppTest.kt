package es.iaaa.kubby

import es.iaaa.kubby.config.dataPath
import es.iaaa.kubby.config.pagePath
import es.iaaa.kubby.config.resourcePath
import es.iaaa.kubby.fixtures.Models.aSimpleModel
import es.iaaa.kubby.repository.DataSource
import io.ktor.config.ApplicationConfig
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withApplication
import org.junit.Before
import org.koin.standalone.inject
import org.koin.test.AutoCloseKoinTest
import org.koin.test.declareMock
import org.mockito.BDDMockito.given
import kotlin.test.Test
import kotlin.test.assertEquals

class KubbyAppTest : AutoCloseKoinTest() {

    val dao by inject<DataSource>()

    lateinit var runtimeConfig: ApplicationConfig

    @Before
    fun before() {
        runtimeConfig = commandLineEnvironment(arrayOf("-port=80")).config
    }

    @Test
    fun testIndex() = withApplication(commandLineEnvironment(emptyArray())){
        with(handleRequest(HttpMethod.Get, "/")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("index", response.content)
        }
    }

    @Test
    fun testResource() = withApplication(commandLineEnvironment(emptyArray())) {
        with(handleRequest(HttpMethod.Get, "${runtimeConfig.resourcePath}/1")) {
            assertEquals(HttpStatusCode.Found, response.status())
            assertEquals("${runtimeConfig.dataPath}/1", response.headers["Location"])
        }
    }

    @Test
    fun testData() = withApplication(commandLineEnvironment(emptyArray())) {
        declareMock<DataSource>()
        given(dao.describe("http://localhost/resource/", "1")).will { aSimpleModel("http://localhost/resource/1") }
        with(handleRequest(HttpMethod.Get, "${runtimeConfig.dataPath}/1")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(
                """
                |{
                |  "@graph" : [ {
                |    "@id" : "http://localhost/data/1",
                |    "rdfs:label" : "RDF description of Jane Doe",
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
    fun testPage() = withApplication(commandLineEnvironment(emptyArray())) {
        with(handleRequest(HttpMethod.Get, "${runtimeConfig.pagePath}/1")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("page", response.content)
        }
    }
}


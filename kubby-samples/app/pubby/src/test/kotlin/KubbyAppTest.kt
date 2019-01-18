package es.iaaa.kubby

import es.iaaa.kubby.content.dataPath
import es.iaaa.kubby.content.pagePath
import es.iaaa.kubby.content.resourcePath
import es.iaaa.kubby.fixtures.Models.aSimpleModel
import es.iaaa.kubby.repository.DataSource
import es.iaaa.kubby.repository.QName
import io.ktor.config.ApplicationConfig
import io.ktor.http.HttpHeaders
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

    private val dao by inject<DataSource>()

    lateinit var runtimeConfig: ApplicationConfig

    @Before fun before() {
        runtimeConfig = commandLineEnvironment(arrayOf("-port=80")).config
    }

    @Test fun `index redirects to DBPedia page`() = withApplication(commandLineEnvironment(emptyArray())) {
        with(handleRequest(HttpMethod.Get, "/")) {
            assertEquals(HttpStatusCode.SeeOther, response.status())
            assertEquals("${runtimeConfig.pagePath}/DBpedia", response.headers[HttpHeaders.Location])
        }
    }

    @Test fun `DBpedia as resource returns redirect`() = withApplication(commandLineEnvironment(emptyArray())) {
        with(handleRequest(HttpMethod.Get, "${runtimeConfig.resourcePath}/1")) {
            assertEquals(HttpStatusCode.Found, response.status())
            assertEquals("${runtimeConfig.dataPath}/1", response.headers["Location"])
        }
    }

    @Test fun `resource description returns JSON-LD`() = withApplication(commandLineEnvironment(emptyArray())) {
        declareMock<DataSource>()
        given(
            dao.describe(
                QName(
                    "http://localhost/resource/",
                    "1"
                )
            )
        ).will { aSimpleModel("http://localhost/resource/1") }
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


    @Test fun testPage() = withApplication(commandLineEnvironment(emptyArray())) {
        with(handleRequest(HttpMethod.Get, "${runtimeConfig.pagePath}/1")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("page", response.content)
        }
    }
}


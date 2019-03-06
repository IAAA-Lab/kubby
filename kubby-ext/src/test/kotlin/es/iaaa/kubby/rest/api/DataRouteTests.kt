package es.iaaa.kubby.rest.api

import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import com.jayway.jsonpath.matchers.JsonPathMatchers.isJson
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import es.iaaa.kubby.fixtures.Models.anEmptyEntity
import es.iaaa.kubby.fixtures.Models.johnSmith
import es.iaaa.kubby.ktor.features.rdf
import es.iaaa.kubby.services.DescribeEntityService
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Routing
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.koin.dsl.module.module
import org.koin.ktor.ext.installKoin
import org.koin.test.AutoCloseKoinTest
import org.koin.test.declareMock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


class DataRouteTests : AutoCloseKoinTest() {

    private lateinit var sut: Application.() -> Unit

    @Suppress("CAST_NEVER_SUCCEEDS")
    @BeforeTest
    fun before() {
        sut = {
            installKoin(listOf(module {
                single { null as DescribeEntityService }
                single { Routes("/p", "/d", "/r") }
            }
            ))
            install(Routing) {
                data()
            }
            install(ContentNegotiation) {
                rdf()
            }
        }
    }

    @Test
    fun `data controller returns the description of a resource with content`() = withTestApplication(sut) {
        declareMock<DescribeEntityService> {
            stub {
                on { findOne("http://localhost/r/", "index") } doReturn johnSmith()
            }
        }

        with(handleRequest(HttpMethod.Get, "/d/index")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertThat(response.content, isJson())
            assertThat(response.content, hasJsonPath("$.@id", equalTo("http://source/JohnSmith")))
            assertThat(response.content, hasJsonPath("$.vcard:FN", equalTo("John Smith")))
        }
    }

    @Test
    fun `data controller do not respond to empty resources`() = withTestApplication(sut) {
        declareMock<DescribeEntityService> {
            stub {
                on { findOne("http://localhost/r/", "index") } doReturn anEmptyEntity()
            }
        }

        with(handleRequest(HttpMethod.Get, "/d/index")) {
            assertNull(response.status())
        }
    }
}
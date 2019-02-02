package es.iaaa.kubby.rest.api

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import es.iaaa.kubby.fixtures.Models.aSimpleResource
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
import org.koin.dsl.module.module
import org.koin.ktor.ext.installKoin
import org.koin.test.AutoCloseKoinTest
import org.koin.test.declareMock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


class PageControllerTests : AutoCloseKoinTest() {

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
                pageController { PageResponse(HttpStatusCode.OK, "textual context") }
            }
            install(ContentNegotiation) {
                rdf()
            }
        }
    }

    @Test
    fun `page controller returns the description of a resource with content`() = withTestApplication(sut) {
        declareMock<DescribeEntityService> {
            stub {
                on { findOne("http://localhost/r/", "index") } doReturn aSimpleResource()
            }
        }

        with(handleRequest(HttpMethod.Get, "/p/index")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("textual context", response.content)
        }
    }
}
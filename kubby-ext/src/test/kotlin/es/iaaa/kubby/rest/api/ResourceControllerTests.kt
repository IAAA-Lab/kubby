package es.iaaa.kubby.rest.api

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Routing
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.koin.dsl.module.module
import org.koin.ktor.ext.installKoin
import org.koin.test.AutoCloseKoinTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


class ResourceControllerTests : AutoCloseKoinTest() {

    private lateinit var sut: Application.() -> Unit

    @BeforeTest
    fun before() {
        sut = {
            installKoin(listOf(module {
                single { Routes("/p", "/d", "/r") }
            }
            ))
            install(Routing) {
                resourceController()
            }
        }
    }

    @Test
    fun `redirect controller redirects text requests to page`() = withTestApplication(sut) {
        with(handleRequest(HttpMethod.Get, "/r/index") { addHeader("Accept", "text/html")}) {
            assertEquals(HttpStatusCode.SeeOther, response.status())
            assertEquals("http://localhost/p/index", response.headers[HttpHeaders.Location])
        }
    }

    @Test
    fun `redirect controller redirects browser requests to page`() = withTestApplication(sut) {
        with(handleRequest(HttpMethod.Get, "/r/index") { addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")}) {
            assertEquals(HttpStatusCode.SeeOther, response.status())
            assertEquals("http://localhost/p/index", response.headers[HttpHeaders.Location])
        }
    }

    @Test
    fun `redirect controller redirects data requests to data`() = withTestApplication(sut) {
        with(handleRequest(HttpMethod.Get, "/r/index") { addHeader("Accept", "application/ld+json")}) {
            assertEquals(HttpStatusCode.SeeOther, response.status())
            assertEquals("http://localhost/d/index", response.headers[HttpHeaders.Location])
        }
    }

    @Test
    fun `redirect controller redirects mixed data and page requests to page`() = withTestApplication(sut) {
        with(handleRequest(HttpMethod.Get, "/r/index") { addHeader("Accept", "application/ld+json,text/html")}) {
            assertEquals(HttpStatusCode.SeeOther, response.status())
            assertEquals("http://localhost/p/index", response.headers[HttpHeaders.Location])
        }
    }
}
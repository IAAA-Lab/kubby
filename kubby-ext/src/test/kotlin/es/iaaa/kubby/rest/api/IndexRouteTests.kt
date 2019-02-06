package es.iaaa.kubby.rest.api

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import es.iaaa.kubby.services.IndexService
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
import org.koin.test.declareMock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


class IndexRouteTests : AutoCloseKoinTest() {

    private lateinit var sut: Application.() -> Unit

    @Suppress("CAST_NEVER_SUCCEEDS")
    @BeforeTest
    fun before() {
        sut = {
            installKoin(listOf(module {
                single { null as IndexService }
                single { Routes("/p", "/d", "/r") }
            }
            ))
            install(Routing) {
                index()
            }
        }
    }

    @Test
    fun `index controller redirects to index page representation`() = withTestApplication(sut) {
        declareMock<IndexService> {
            stub {
                on { indexLocalPart() } doReturn "index"
            }
        }

        with(handleRequest(HttpMethod.Get, "/")) {
            assertEquals(HttpStatusCode.SeeOther, response.status())
            assertEquals("http://localhost/p/index", response.headers[HttpHeaders.Location])
        }
    }

    @Test
    fun `null local part implies index controller do nothing`() = withTestApplication(sut) {
        declareMock<IndexService> {
            stub {
                on { indexLocalPart() } doReturn null
            }
        }

        with(handleRequest(HttpMethod.Get, "/")) {
            assertNull(response.status())
        }
    }

}
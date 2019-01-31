package es.iaaa.kubby.rest.api

import io.ktor.application.ApplicationCall
import io.ktor.features.origin
import io.ktor.http.RequestConnectionPoint
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class ControllersTest {

    @Test
    fun `authority for standard http request is the host`() {
        val request = mockk<RequestConnectionPoint>()

        every { request.scheme } returns "http"
        every { request.host } returns "localhost"
        every { request.port } returns 80

        assertEquals("localhost", request.authority)
    }

    @Test
    fun `authority for http request in a port different from 80 is the host plus the port`() {
        val request = mockk<RequestConnectionPoint>()

        every { request.scheme } returns "http"
        every { request.host } returns "localhost"
        every { request.port } returns 8080

        assertEquals("localhost:8080", request.authority)
    }

    @Test
    fun `authority for standard https request is the host`() {
        val request = mockk<RequestConnectionPoint>()

        every { request.scheme } returns "https"
        every { request.host } returns "localhost"
        every { request.port } returns 443

        assertEquals("localhost", request.authority)
    }

    @Test
    fun `authority for https request in a port different from 443 is the host plus the port`() {
        val request = mockk<RequestConnectionPoint>()

        every { request.scheme } returns "https"
        every { request.host } returns "localhost"
        every { request.port } returns 8443

        assertEquals("localhost:8443", request.authority)
    }

    @Test
    fun `extract hier part rebuilds the original request`() {
        val applicationCall = mockk<ApplicationCall>()

        every { applicationCall.request.origin.scheme } returns "http"
        every { applicationCall.request.origin.host } returns "localhost"
        every { applicationCall.request.origin.port } returns 80
        every { applicationCall.request.origin.uri } returns "/example"

        assertEquals("http://localhost/example", applicationCall.extractHierPart(""))
    }

    @Test
    fun `extract hier part keeps the original request if the local part is different`() {
        val applicationCall = mockk<ApplicationCall>()

        every { applicationCall.request.origin.scheme } returns "http"
        every { applicationCall.request.origin.host } returns "localhost"
        every { applicationCall.request.origin.port } returns 80
        every { applicationCall.request.origin.uri } returns "/example"

        assertEquals("http://localhost/example", applicationCall.extractHierPart("diff"))
    }

    @Test
    fun `extract hier part keeps only part the original request if the local part is suffix`() {
        val applicationCall = mockk<ApplicationCall>()

        every { applicationCall.request.origin.scheme } returns "http"
        every { applicationCall.request.origin.host } returns "localhost"
        every { applicationCall.request.origin.port } returns 80
        every { applicationCall.request.origin.uri } returns "/example"

        assertEquals("http://localhost/ex", applicationCall.extractHierPart("ample"))
    }


    @Test
    fun `extract local path with multiple segments`() {
        val applicationCall = mockk<ApplicationCall>()

        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns listOf("a", "b")

        assertEquals("a/b", applicationCall.extractLocalPath())
    }

    @Test
    fun `escape question marks in local paths`() {
        val applicationCall = mockk<ApplicationCall>()

        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns listOf("a?")

        assertEquals("a%3F", applicationCall.extractLocalPath())
    }


    @Test
    fun `extract local path with single segments`() {
        val applicationCall = mockk<ApplicationCall>()

        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns listOf("a")

        assertEquals("a", applicationCall.extractLocalPath())
    }

    @Test
    fun `extract local path with no segments`() {
        val applicationCall = mockk<ApplicationCall>()

        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns emptyList()

        assertEquals("", applicationCall.extractLocalPath())
    }

    @Test
    fun `return empty string if no local path`() {
        val applicationCall = mockk<ApplicationCall>()

        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns null

        assertEquals("", applicationCall.extractLocalPath())
    }

    @Test
    fun `extract entity uris`() {
        val applicationCall = mockk<ApplicationCall>()
        val routes = Routes(dataPath = "/d", resourcePath = "/r", pagePath = "/p")

        every { applicationCall.request.origin.scheme } returns "http"
        every { applicationCall.request.origin.host } returns "localhost"
        every { applicationCall.request.origin.port } returns 80
        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns listOf("1")

        for (i in listOf("d", "r", "p")) {
            every { applicationCall.request.origin.uri } returns "/$i/1"

            val uris = applicationCall.extractEntityUris(i, routes)

            assertEquals("http://localhost/d/1", uris.data)
            assertEquals("http://localhost/p/1", uris.page)
            assertEquals("http://localhost/r/", uris.namespace)
            assertEquals("1", uris.localId)
        }

    }

    @Test
    fun `extract selector uris with slash`() {
        val applicationCall = mockk<ApplicationCall>()
        val routes = Routes(dataPath = "/d", resourcePath = "/r", pagePath = "/p")

        every { applicationCall.request.origin.scheme } returns "http"
        every { applicationCall.request.origin.host } returns "localhost"
        every { applicationCall.request.origin.port } returns 80
        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns null

        for (i in listOf("d", "r", "p")) {
            every { applicationCall.request.origin.uri } returns "/$i/"

            val uris = applicationCall.extractEntityUris(i, routes)

            assertEquals("http://localhost/d/", uris.data)
            assertEquals("http://localhost/p/", uris.page)
            assertEquals("http://localhost/r/", uris.namespace)
            assertEquals("", uris.localId)
        }

    }

    @Test
    fun `extract selector uris without slash`() {
        val applicationCall = mockk<ApplicationCall>()
        val routes = Routes(dataPath = "/d", resourcePath = "/r", pagePath = "/p")

        every { applicationCall.request.origin.scheme } returns "http"
        every { applicationCall.request.origin.host } returns "localhost"
        every { applicationCall.request.origin.port } returns 80
        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns null

        for (i in listOf("d", "r", "p")) {
            every { applicationCall.request.origin.uri } returns "/$i"

            val uris = applicationCall.extractEntityUris(i, routes)

            assertEquals("http://localhost/d/", uris.data)
            assertEquals("http://localhost/p/", uris.page)
            assertEquals("http://localhost/r/", uris.namespace)
            assertEquals("", uris.localId)
        }

    }
}
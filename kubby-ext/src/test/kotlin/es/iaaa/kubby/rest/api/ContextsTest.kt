package es.iaaa.kubby.rest.api

import es.iaaa.kubby.services.DescribeEntityService
import io.ktor.application.ApplicationCall
import io.ktor.features.origin
import io.ktor.http.RequestConnectionPoint
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.jena.rdf.model.ModelFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContextsTest {

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
    fun `extract loal path with multiple segments`() {
        val applicationCall = mockk<ApplicationCall>()

        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns listOf("a", "b")

        assertEquals("a/b", applicationCall.extractLocalPath(PATH_LOCAL_PART))
    }

    @Test
    fun `escape question marks in local paths`() {
        val applicationCall = mockk<ApplicationCall>()

        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns listOf("a?")

        assertEquals("a%3F", applicationCall.extractLocalPath(PATH_LOCAL_PART))
    }


    @Test
    fun `extract local path with single segments`() {
        val applicationCall = mockk<ApplicationCall>()

        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns listOf("a")

        assertEquals("a", applicationCall.extractLocalPath(PATH_LOCAL_PART))
    }

    @Test
    fun `extract local path with no segments`() {
        val applicationCall = mockk<ApplicationCall>()

        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns emptyList()

        assertEquals("", applicationCall.extractLocalPath(PATH_LOCAL_PART))
    }

    @Test
    fun `return empty string if no local path`() {
        val applicationCall = mockk<ApplicationCall>()

        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns null

        assertEquals("", applicationCall.extractLocalPath(PATH_LOCAL_PART))
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

            val uris = applicationCall.extractEntityUris(PATH_LOCAL_PART, i, routes)

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

            val uris = applicationCall.extractEntityUris(PATH_LOCAL_PART, i, routes)

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

            val uris = applicationCall.extractEntityUris(PATH_LOCAL_PART, i, routes)

            assertEquals("http://localhost/d/", uris.data)
            assertEquals("http://localhost/p/", uris.page)
            assertEquals("http://localhost/r/", uris.namespace)
            assertEquals("", uris.localId)
        }

    }


    @Test
    fun `process request with id is findable`() {
        val applicationCall = mockk<ApplicationCall>()
        val service = mockk<DescribeEntityService>()

        val routes = Routes(dataPath = "/d", resourcePath = "/r", pagePath = "/p")
        val resource = anyResource()

        every { applicationCall.request.origin.scheme } returns "http"
        every { applicationCall.request.origin.host } returns "localhost"
        every { applicationCall.request.origin.port } returns 80
        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns listOf("1")
        every { service.findOne("http://localhost/r/", "1") } returns resource

        for (i in listOf("d", "p")) {
            every { applicationCall.request.origin.uri } returns "/$i/1"

            val ctx = applicationCall.processRequests(PATH_LOCAL_PART,"/$i", routes, service)


            assertTrue(ctx is ContentContext)
            assertEquals("http://localhost/d/1", ctx.data)
            assertEquals("http://localhost/p/1", ctx.page)
            assertEquals(resource, ctx.resource)
        }

        verify(exactly = 2) { service.findOne("http://localhost/r/", "1") }
    }

    @Test
    fun `process request without id is not findable`() {
        val applicationCall = mockk<ApplicationCall>()
        val service = mockk<DescribeEntityService>()

        val routes = Routes(dataPath = "/d", resourcePath = "/r", pagePath = "/p")
        val resource = anyResource()

        every { applicationCall.request.origin.scheme } returns "http"
        every { applicationCall.request.origin.host } returns "localhost"
        every { applicationCall.request.origin.port } returns 80
        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns null

        for (i in listOf("d", "p")) {
            every { applicationCall.request.origin.uri } returns "/$i/"
            every { service.findOne("http://localhost/r/", "") } returns resource

            val ctx = applicationCall.processRequests(PATH_LOCAL_PART,"/$i", routes, service)

            verify(exactly = 0) { service.findOne("http://localhost/r/", "") }

            assertTrue(ctx is NoContext)
        }
    }

    @Test
    fun `resource redirect with id is findable`() {
        val applicationCall = mockk<ApplicationCall>()
        val routes = Routes(dataPath = "/d", resourcePath = "/r", pagePath = "/p")

        every { applicationCall.request.origin.scheme } returns "http"
        every { applicationCall.request.origin.host } returns "localhost"
        every { applicationCall.request.origin.port } returns 80
        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns listOf("1")
        every { applicationCall.request.origin.uri } returns "/r/1"

        val ctx = applicationCall.processRedirects(PATH_LOCAL_PART, routes)

        assertTrue(ctx is RedirectContext)
        assertEquals("http://localhost/d/1", ctx.data)
        assertEquals("http://localhost/p/1", ctx.page)
    }

    @Test
    fun `resource redirect without id is not findable`() {
        val applicationCall = mockk<ApplicationCall>()
        val routes = Routes(dataPath = "/d", resourcePath = "/r", pagePath = "/p")

        every { applicationCall.request.origin.scheme } returns "http"
        every { applicationCall.request.origin.host } returns "localhost"
        every { applicationCall.request.origin.port } returns 80
        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns null
        every { applicationCall.request.origin.uri } returns "/r/"

        val ctx = applicationCall.processRedirects(PATH_LOCAL_PART, routes)

        assertTrue(ctx is NoContext)
    }

    @Test
    fun `resource redirect without id and slash is not findable`() {
        val applicationCall = mockk<ApplicationCall>()
        val routes = Routes(dataPath = "/d", resourcePath = "/r", pagePath = "/p")

        every { applicationCall.request.origin.scheme } returns "http"
        every { applicationCall.request.origin.host } returns "localhost"
        every { applicationCall.request.origin.port } returns 80
        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns null
        every { applicationCall.request.origin.uri } returns "/r"

        val ctx = applicationCall.processRedirects(PATH_LOCAL_PART, routes)

        assertTrue(ctx is NoContext)
    }

    @Test
    fun `index redirect is findable`() {
        val applicationCall = mockk<ApplicationCall>()
        val routes = Routes(dataPath = "/d", resourcePath = "/r", pagePath = "/p")

        every { applicationCall.request.origin.scheme } returns "http"
        every { applicationCall.request.origin.host } returns "localhost"
        every { applicationCall.request.origin.port } returns 80
        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns null
        every { applicationCall.request.origin.uri } returns "/"

        val ctx = applicationCall.processRedirects(PATH_LOCAL_PART, routes, "1")

        assertTrue(ctx is RedirectContext)
        assertEquals("http://localhost/d/1", ctx.data)
        assertEquals("http://localhost/p/1", ctx.page)
    }

    @Test
    fun `index redirect and resource redirect is not findable`() {
        val applicationCall = mockk<ApplicationCall>()
        val routes = Routes(dataPath = "/d", resourcePath = "/r", pagePath = "/p")

        every { applicationCall.request.origin.scheme } returns "http"
        every { applicationCall.request.origin.host } returns "localhost"
        every { applicationCall.request.origin.port } returns 80
        every { applicationCall.parameters.getAll(PATH_LOCAL_PART) } returns listOf("2")
        every { applicationCall.request.origin.uri } returns "/r/2"

        val ctx = applicationCall.processRedirects(PATH_LOCAL_PART, routes, "1")

        assertTrue(ctx is NoContext)
    }

    fun anyResource() = ModelFactory.createDefaultModel().createResource()
}


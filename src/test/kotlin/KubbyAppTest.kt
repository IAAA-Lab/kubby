package es.iaaa.kubby

import es.iaaa.kubby.sources.DataSource
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.mockk.every
import io.mockk.mockk
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.RDFS
import kotlin.test.Test
import kotlin.test.assertEquals


class ApplicationTest {

    private val dao = mockk<DataSource>(relaxed = true)


    @Test
    fun testIndex() = testApp {
        handleRequest(HttpMethod.Get, "/").apply {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("index", response.content)
        }
    }

    @Test
    fun testResource() = testApp {
        handleRequest(HttpMethod.Get, "/resource/1").apply {
            assertEquals(HttpStatusCode.Found, response.status())
            assertEquals("/data/1", response.headers["Location"])
        }
    }

    @Test
    fun testData() = testApp {
        every { dao.describe("1") } returns aSimpleModel()

        handleRequest(HttpMethod.Get, "/data/1").apply {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("data", response.content)
        }
    }

    @Test
    fun testPage() = testApp {
        handleRequest(HttpMethod.Get, "/page/1").apply {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("page", response.content)
        }
    }

    private fun testApp(callback: TestApplicationEngine.() -> Unit) {
        withTestApplication({ mainWithDependencies(dao) }) { callback() }
    }

    private fun aSimpleModel(): Model {
        val m = ModelFactory.createDefaultModel()
        val ns = "http://schema.org/"
        val person = m.createResource(ns + "Person")
        m.setNsPrefix("schema", "http://schema.org/")
        m.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
        m.createResource("http://www.ex.com/janedoe").let {
            m.add(it, m.createProperty(ns, "name"), "Jane Doe")
            m.add(it, RDF.type, person)
            m.add(it, RDFS.seeAlso, m.createResource("http://www.ex.com/janedoe/moreinfo"))
            m.add(it, m.createProperty(ns, "url"), "http://www.janedoe.com")
            m.add(it, m.createProperty(ns, "jobTitle"), "Professor")
        }
        return m
    }

}


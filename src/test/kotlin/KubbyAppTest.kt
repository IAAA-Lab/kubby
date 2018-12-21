package es.iaaa.kubby

//import io.mockk.every
//import io.mockk.mockk
import es.iaaa.kubby.repository.DataSource
import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.RDFS
import org.junit.Before
import org.koin.standalone.StandAloneContext
import org.koin.standalone.inject
import org.koin.test.AutoCloseKoinTest
import org.koin.test.declareMock
import org.mockito.BDDMockito.given
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest : AutoCloseKoinTest() {

    private val dao: DataSource by inject()

    @Before
    fun before() {
        StandAloneContext.startKoin(listOf(kubbyModule))
        declareMock<DataSource>()
    }


//    private val dao = mockk<DataSource>(relaxed = true)
//    private val dao = EmptyDataSource()


    @Test
    fun testIndex() = withTestApplication (Application::main) {
        with(handleRequest(HttpMethod.Get, "/")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("index", response.content)
        }
    }

    @Test
    fun testResource() = withTestApplication (Application::main) {
        with(handleRequest(HttpMethod.Get, "/resource/1")) {
            assertEquals(HttpStatusCode.Found, response.status())
            assertEquals("/data/1", response.headers["Location"])
        }
    }

    @Test
    fun testData() = withTestApplication (Application::main) {
        given(dao.describe("1")).will { aSimpleModel() }
        with(handleRequest(HttpMethod.Get, "/data/1")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("data", response.content)
        }
    }

    @Test
    fun testPage() = withTestApplication (Application::main) {
        with(handleRequest(HttpMethod.Get, "/page/1")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("page", response.content)
        }
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


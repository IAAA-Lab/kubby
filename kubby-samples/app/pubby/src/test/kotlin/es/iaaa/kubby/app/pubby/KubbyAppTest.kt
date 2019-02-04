package es.iaaa.kubby.app.pubby

import es.iaaa.kubby.config.toRoutes
import es.iaaa.kubby.repository.EntityRepository
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
import kotlin.test.Test
import kotlin.test.assertEquals

class KubbyAppTest : AutoCloseKoinTest() {

    private val dao by inject<EntityRepository>()

    lateinit var runtimeConfig: ApplicationConfig

    @Before
    fun before() {
        runtimeConfig = commandLineEnvironment(arrayOf("-port=80")).config
    }

    @Test
    fun `index redirects to DBPedia page`() = withApplication(commandLineEnvironment(emptyArray())) {
        with(handleRequest(HttpMethod.Get, "/")) {
            assertEquals(HttpStatusCode.SeeOther, response.status())
            assertEquals(
                "http://localhost${runtimeConfig.toRoutes().pagePath}/DBpedia",
                response.headers[HttpHeaders.Location]
            )
        }
    }

    @Test
    fun `DBpedia as resource returns redirect`() = withApplication(commandLineEnvironment(emptyArray())) {
        with(handleRequest(HttpMethod.Get, "${runtimeConfig.toRoutes().resourcePath}/1")) {
            assertEquals(HttpStatusCode.SeeOther, response.status())
            assertEquals(
                "http://localhost${runtimeConfig.toRoutes().pagePath}/1",
                response.headers["Location"]
            )
        }
    }



    @Test
    fun testPage() = withApplication(commandLineEnvironment(emptyArray())) {
        with(handleRequest(HttpMethod.Get, "${runtimeConfig.toRoutes().pagePath}/1")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("page", response.content)
        }
    }
}


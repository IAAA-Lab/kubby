package es.iaaa.kubby.app.pubby

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withApplication
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.RDFS
import org.koin.test.AutoCloseKoinTest
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KubbyAppRealDataTest : AutoCloseKoinTest() {

    @Test
    fun testData() = withApplication(commandLineEnvironment(emptyArray())) {
        with(handleRequest(HttpMethod.Get, "dataUri/Tetris")) {
            assertEquals(HttpStatusCode.OK, response.status())
            val model = ModelFactory.createDefaultModel()
            RDFDataMgr.read(model, StringReader(response.content), null, Lang.JSONLD)
            assertTrue(
                model.contains(
                    model.createResource("http://localhost/resource/Tetris"),
                    RDFS.seeAlso, model.createResource("http://localhost/resource/Korobeiniki")
                )
            )
            assertTrue(
                model.contains(
                    model.createResource("http://localhost/dataUri/Tetris"),
                    FOAF.primaryTopic, model.createResource("http://localhost/resource/Tetris")
                )
            )
        }
    }
}


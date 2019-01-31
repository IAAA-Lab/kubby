package es.iaaa.kubby.ktor.features

import com.github.jsonldjava.core.JsonLdOptions
import es.iaaa.kubby.fixtures.Models
import org.apache.jena.riot.RDFFormat
import org.json.JSONObject
import kotlin.test.Test
import kotlin.test.assertEquals


class ContentNegotiationTest {

    @Test
    fun `generates JSON-LD`() {
        val obj = JSONObject(Models.aSimpleModel().toString(RDFFormat.JSONLD, JsonLdOptions()))
        assertEquals("http://source/JohnSmith", obj["@id"])
        assertEquals("John Smith", obj["http://www.w3.org/2001/vcard-rdf/3.0#FN"])
    }
}
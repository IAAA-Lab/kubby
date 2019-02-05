package es.iaaa.kubby.ktor.features

import com.github.jsonldjava.core.JsonLdOptions
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import com.jayway.jsonpath.matchers.JsonPathMatchers.isJson
import es.iaaa.kubby.fixtures.Models
import org.apache.jena.riot.RDFFormat
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import kotlin.test.Test


class ContentNegotiationTest {

    @Test
    fun `generates JSON-LD`() {
        val obj = Models.johnSmithModel().toString(RDFFormat.JSONLD, JsonLdOptions())
        assertThat(obj, isJson())
        assertThat(obj, hasJsonPath("$.@id", equalTo("http://source/JohnSmith")))
        assertThat(obj, hasJsonPath("$.vcard:FN", equalTo("John Smith")))
    }
}
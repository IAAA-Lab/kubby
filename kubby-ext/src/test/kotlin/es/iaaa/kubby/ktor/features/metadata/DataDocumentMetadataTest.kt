package es.iaaa.kubby.ktor.features.metadata

import es.iaaa.kubby.config.ProjectDescription
import es.iaaa.kubby.rdf.ask
import es.iaaa.kubby.rest.api.DataContentContext
import io.mockk.every
import io.mockk.mockk
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.RDFS
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class DataDocumentMetadataTest {
    private lateinit var model: Model
    private lateinit var context: DataContentContext
    private lateinit var project: ProjectDescription

    @BeforeTest
    fun before() {
        model = ModelFactory.createDefaultModel()
        context = mockk()
        project = mockk()

        every { context.entity.resource.uri } returns "http://localhost/resource/ResourceOfSomething"
        every { project.labelProperties } returns listOf(RDFS.label)
        every { project.defaultLanguage } returns "en"
        every { project.getLanguageList("uncapitalized-words") } returns listOf("of", "an")
        every { project.getLanguageValue("metadata-document-label", "en") } returns "Description of %1\$s"
        every { project.getLanguageValue("metadata-document-label-anon", "en") } returns "an unnamed resource"
        every { context.dataUri } returns "http://localhost/dataUri/DocumentAboutSomething"
    }

    @Test
    fun `the document has a primary topic`() {
        DocumentMetadata().process(model, context, project)

        val query = """
            PREFIX foaf: <http://xmlns.com/foaf/0.1/>
            PREFIX dataUri: <http://localhost/dataUri/>
            PREFIX resource: <http://localhost/resource/>
            ASK
            {
                dataUri:DocumentAboutSomething foaf:primaryTopic resource:ResourceOfSomething
            }
        """.trimIndent()
        assertTrue(model ask query)
    }

    @Test
    fun `the document has a human readable label when the resource does not provides one`() {
        DocumentMetadata().process(model, context, project)

        val query = """
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX dataUri: <http://localhost/dataUri/>
            ASK
            {
                dataUri:DocumentAboutSomething rdfs:label "Description of Resource of Something"
            }
        """.trimIndent()
        assertTrue(model ask query)
    }

    @Test
    fun `the document has a human readable label provided by the resource`() {
        model.apply {
            add(createResource("http://localhost/resource/ResourceOfSomething"), RDFS.label, "Title")
        }
        DocumentMetadata().process(model, context, project)

        val query = """
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX dataUri: <http://localhost/dataUri/>
            ASK
            {
                dataUri:DocumentAboutSomething rdfs:label "Description of Title"
            }
        """.trimIndent()
        assertTrue(model ask query)
    }

    @Test
    fun `the document has a human readable label even when the provided by the resource is unusable`() {
        model.apply {
            add(createResource("http://localhost/resource/ResourceOfSomething"), RDFS.label, "")
        }
        DocumentMetadata().process(model, context, project)

        val query = """
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX dataUri: <http://localhost/dataUri/>
            ASK
            {
                dataUri:DocumentAboutSomething rdfs:label "Description of an unnamed resource"
            }
        """.trimIndent()
        assertTrue(model ask query)
    }
}
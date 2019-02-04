package es.iaaa.kubby.ktor.features.metadata

import es.iaaa.kubby.config.ProjectDescription
import es.iaaa.kubby.rest.api.ContentContext
import es.iaaa.kubby.vocabulary.PROV
import io.mockk.every
import io.mockk.mockk
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.QueryFactory
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.RDFS
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class ProvenanceMetadataTest {

    private lateinit var model: Model
    private lateinit var context: ContentContext
    private lateinit var project: ProjectDescription

    @BeforeTest
    fun before() {
        model = ModelFactory.createDefaultModel()
        context = mockk()
        project = mockk()

        every { context.time } returns GregorianCalendar.getInstance()
        every { context.page } returns "http://localhost/document"
        every { project.softwareName } returns "software"
    }

    @Test
    fun `the document was generated by software in an activity`() {
        ProvenanceMetadata().process(model, context, project)

        val query = """
            PREFIX prov: <${PROV.uri}>
            PREFIX local: <http://localhost/>
            ASK
            {
                ?x a prov:Activity .
                ?x prov:generated local:document .
                ?x prov:wasAssociatedWith [ a prov:SoftwareAgent ] .
            }
        """.trimIndent()
        assertTrue(query.ask(model))
    }

    @Test
    fun `the software agent has a name`() {
        ProvenanceMetadata().process(model, context, project)

        val query = """
            PREFIX prov: <${PROV.uri}>
            PREFIX rdfs: <${RDFS.uri}>
            ASK
            {
                ?x a prov:SoftwareAgent .
                ?x rdfs:label "software"
            }
        """.trimIndent()
        assertTrue(query.ask(model))
    }

    @Test
    fun `the document is an entity generated by an activity in a date`() {
        ProvenanceMetadata().process(model, context, project)

        val query = """
            PREFIX prov: <${PROV.uri}>
            PREFIX local: <http://localhost/>
            PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
            ASK
            {
                local:document a prov:Entity .
                local:document prov:wasGeneratedBy [a prov:Activity] .
                local:document prov:generatedAtTime ?z . FILTER (datatype(?z) = xsd:dateTime )
            }
        """.trimIndent()
        assertTrue(query.ask(model))
    }

    fun String.ask(model: Model) = QueryExecutionFactory.create(QueryFactory.create(this), model).execAsk()
}
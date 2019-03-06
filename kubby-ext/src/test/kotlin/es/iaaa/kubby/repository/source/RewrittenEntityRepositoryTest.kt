package es.iaaa.kubby.repository.source

import es.iaaa.kubby.domain.impl.ResourceEntityImpl
import es.iaaa.kubby.fixtures.Models.johnSmithModel
import es.iaaa.kubby.rdf.ask
import es.iaaa.kubby.repository.EntityRepository
import io.mockk.every
import io.mockk.mockk
import org.apache.jena.rdf.model.Model
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RewrittenEntityRepositoryTest {

    private val anyDs: EntityRepository = mockk(relaxed = true)

    @BeforeTest
    fun before() {
        every { anyDs.findOne("http://source/", "JohnSmith") } returns ResourceEntityImpl("http://source/JohnSmith", johnSmithModel())
    }

    @Test
    fun `describe and rewrite a simple model`() {
        val rdw = RewrittenEntityRepository(anyDs, "http://source/", false)
        val entity = rdw.findOne("http://target/", "JohnSmith")
        val query = """
            PREFIX target: <http://target/>
            PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#>
            ASK {
                target:JohnSmith vcard:FN "John Smith"
            }
        """.trimIndent()
        assertTrue((entity.toGraphModel() as Model).ask(query))
    }

    @Test
    fun `verify that the rewritten source doesn't have an unrequested owl sameAs`() {
        assertFalse(testSameAs(false))
    }

    @Test
    fun `verify that the rewritten source has the expected owl sameAs`() {
        assertTrue(testSameAs(true))
    }

    private fun testSameAs(addSameAs: Boolean): Boolean {
        val rdw = RewrittenEntityRepository(anyDs, "http://source/", addSameAs)
        val entity = rdw.findOne("http://target/", "JohnSmith")
        val query = """
            PREFIX target: <http://target/>
            PREFIX source: <http://source/>
            PREFIX owl: <http://www.w3.org/2002/07/owl#>
            ASK {
                target:JohnSmith owl:sameAs source:JohnSmith
            }
        """.trimIndent()
        return (entity.toGraphModel() as Model).ask(query)
    }

}
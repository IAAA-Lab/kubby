package es.iaaa.kubby.repository.source

import es.iaaa.kubby.fixtures.Models.johnSmithModel
import es.iaaa.kubby.rdf.ask
import es.iaaa.kubby.repository.EntityId
import es.iaaa.kubby.repository.EntityRepository
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RewrittenEntityRepositoryTest {

    private val anyDs: EntityRepository = mockk(relaxed = true)

    @BeforeTest
    fun before() {
        every {
            anyDs.findOne(
                EntityId(
                    "http://source/",
                    "JohnSmith"
                )
            )
        } returns johnSmithModel().getResource("http://source/JohnSmit")
    }

    @Test
    fun `describe and rewrite a simple model`() {
        val rdw = RewrittenEntityRepository(anyDs, "http://source/", false)
        val result = rdw.findOne(EntityId("http://target/", "JohnSmith"))
        val query = """
            PREFIX target: <http://target/>
            PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#>
            ASK {
                target:JohnSmith vcard:FN "John Smith"
            }
        """.trimIndent()
        assertTrue(result.model.ask(query))
    }

    @Test
    fun `verify that the rewritten source doesn't have an unrequested owl sameAs`() {
        val rdw = RewrittenEntityRepository(anyDs, "http://source/", false)
        val result = rdw.findOne(EntityId("http://target/", "JohnSmith"))
        val query = """
            PREFIX target: <http://target/>
            PREFIX source: <http://source/>
            PREFIX owl: <http://www.w3.org/2002/07/owl#>
            ASK {
                target:JohnSmith owl:sameAs source:JohnSmith
            }
        """.trimIndent()
        assertFalse(result.model.ask(query))
    }

    @Test
    fun `verify that the rewritten source has the expected owl sameAs`() {
        val rdw = RewrittenEntityRepository(anyDs, "http://source/", true)
        val result = rdw.findOne(EntityId("http://target/", "JohnSmith"))
        val query = """
            PREFIX target: <http://target/>
            PREFIX source: <http://source/>
            PREFIX owl: <http://www.w3.org/2002/07/owl#>
            ASK {
                target:JohnSmith owl:sameAs source:JohnSmith
            }
        """.trimIndent()
        assertTrue(result.model.ask(query))
    }

}
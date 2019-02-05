package es.iaaa.kubby.repository.source

import es.iaaa.kubby.fixtures.Models.johnSmithModel
import es.iaaa.kubby.repository.EntityId
import es.iaaa.kubby.repository.EntityRepository
import io.mockk.every
import io.mockk.mockk
import org.apache.jena.vocabulary.OWL
import org.apache.jena.vocabulary.VCARD
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
        result.model.apply {
            assertTrue(
                contains(
                    createResource("http://target/JohnSmith"),
                    VCARD.FN,
                    "John Smith"
                )
            )
        }
    }

    @Test
    fun `verify that the rewritten source doesn't have an unrequested owl sameAs`() {
        val rdw = RewrittenEntityRepository(anyDs, "http://source/", false)
        val result = rdw.findOne(EntityId("http://target/", "JohnSmith"))
        result.model.apply {
            assertFalse(
                contains(
                    createResource("http://target/JohnSmith"),
                    OWL.sameAs,
                    createResource("http://source/JohnSmith")
                )
            )
        }
    }

    @Test
    fun `verify that the rewritten source has the expected owl sameAs`() {
        val rdw = RewrittenEntityRepository(anyDs, "http://source/", true)
        val result = rdw.findOne(EntityId("http://target/", "JohnSmith"))
        result.model.apply {
            assertTrue(
                contains(
                    createResource("http://target/JohnSmith"),
                    OWL.sameAs,
                    createResource("http://source/JohnSmith")
                )
            )
        }
    }

}
package es.iaaa.kubby.repository.source

import es.iaaa.kubby.fixtures.Models.emptyEntity
import es.iaaa.kubby.fixtures.Models.johnSmith
import es.iaaa.kubby.fixtures.Models.marySmithAboutJohnSmith
import es.iaaa.kubby.rdf.ask
import es.iaaa.kubby.repository.EntityRepository
import io.mockk.every
import io.mockk.mockk
import org.apache.jena.rdf.model.Model
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MergeEntityRepositoryTest {

    lateinit var emptyRepository: EntityRepository
    lateinit var johnRepository: EntityRepository
    lateinit var maryRepository: EntityRepository


    @BeforeTest
    fun before() {
        emptyRepository = mockk()
        every { emptyRepository.findOne("http://source/", "JohnSmith") } returns emptyEntity("http://source/JohnSmith")

        johnRepository = mockk()
        every { johnRepository.findOne("http://source/", "JohnSmith") } returns johnSmith()

        maryRepository = mockk()
        every { maryRepository.findOne("http://source/", "JohnSmith") } returns marySmithAboutJohnSmith()
    }


    @Test
    fun `empty lists never fails and returns an empty model`() {
        val repository = MergeEntityRepository(listOf())
        val entity = repository.findOne("http://source/", "JohnSmith")
        assertEquals("http://source/JohnSmith", entity.uri)
        assertTrue(entity.isEmpty)
    }

    @Test
    fun `if no data is found, returns an empty model`() {
        val repository = MergeEntityRepository(listOf(emptyRepository))
        val entity = repository.findOne("http://source/", "JohnSmith")
        assertEquals("http://source/JohnSmith", entity.uri)
        assertTrue(entity.isEmpty)
    }

    @Test
    fun `returns information about an entity`() {
        val repository = MergeEntityRepository(listOf(johnRepository))
        val entity = repository.findOne("http://source/", "JohnSmith")
        assertEquals("http://source/JohnSmith", entity.uri)
        val query = """
            PREFIX src: <http://source/>
            PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#>
            ASK {
                src:JohnSmith vcard:FN "John Smith"
            }
        """.trimIndent()
        assertTrue((entity.toGraphModel() as Model).ask(query))
    }

    @Test
    fun `information about an entity in two repositories is merged`() {
        val repository = MergeEntityRepository(listOf(johnRepository, maryRepository))
        val entity = repository.findOne("http://source/", "JohnSmith")
        assertEquals("http://source/JohnSmith", entity.uri)
        val query = """
            PREFIX src: <http://source/>
            PREFIX dc: <http://purl.org/dc/elements/1.1/>
            PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#>
            ASK {
                src:JohnSmith dc:relation ?x .
                ?x vcard:FN "Mary Smith"
            }
        """.trimIndent()
        assertTrue((entity.toGraphModel() as Model).ask(query))
    }
}
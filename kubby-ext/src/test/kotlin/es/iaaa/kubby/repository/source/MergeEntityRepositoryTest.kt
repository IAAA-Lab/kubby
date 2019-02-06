package es.iaaa.kubby.repository.source

import es.iaaa.kubby.fixtures.Models.johnSmith
import es.iaaa.kubby.fixtures.Models.marySmith
import es.iaaa.kubby.rdf.ask
import es.iaaa.kubby.repository.EntityId
import es.iaaa.kubby.repository.EntityRepository
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MergeEntityRepositoryTest {

    lateinit var dbpediaRepository: EntityRepository
    lateinit var dbpediaAltRepository: EntityRepository
    lateinit var emptyRepository: EntityRepository
    lateinit var johnRepository: EntityRepository
    lateinit var maryRepository: EntityRepository

    val dbpedia = EntityId("http://localhost/resource/", "DBpedia")
    val dbpediaAlt = EntityId("http://localhost/resource/", "DBpediaAlt")
    val dbpediaRaw = EntityId(localPart = "http://localhost/resource/DBpedia")
    val johnSmithId = EntityId("http://source/", "JohnSmith")


    @BeforeTest
    fun before() {
        dbpediaRepository = mockk()
        every { dbpediaRepository.getId("http://localhost/resource/DBpedia") } returns dbpedia

        dbpediaAltRepository = mockk()
        every { dbpediaAltRepository.getId("http://localhost/resource/DBpedia") } returns dbpediaAlt

        emptyRepository = mockk()
        emptyRepository.apply {
            every { getId("http://localhost/resource/DBpedia") } returns dbpediaRaw
            every { findOne(johnSmithId).model } returns null
        }

        johnRepository = mockk()
        every { johnRepository.findOne(johnSmithId) } returns johnSmith()

        maryRepository = mockk()
        every { maryRepository.findOne(johnSmithId) } returns marySmith()
    }

    @Test
    fun `converts a string into an EntityId`() {
        val repository = MergeEntityRepository(listOf(dbpediaRepository))
        assertEquals(dbpedia, repository.getId("http://localhost/resource/DBpedia"))
    }

    @Test
    fun `returns first match that has content`() {
        val repository = MergeEntityRepository(listOf(dbpediaAltRepository, dbpediaRepository))
        assertEquals(dbpediaAlt, repository.getId("http://localhost/resource/DBpedia"))
    }

    @Test
    fun `empty repository returns an entity without namespace`() {
        val repository = MergeEntityRepository(listOf(emptyRepository))
        assertEquals(dbpediaRaw, repository.getId("http://localhost/resource/DBpedia"))
    }

    @Test
    fun `empty list returns an entity without namespace`() {
        val repository = MergeEntityRepository(listOf())
        assertEquals(dbpediaRaw, repository.getId("http://localhost/resource/DBpedia"))
    }


    @Test
    fun `empty lists never fails and returns an empty model`() {
        val repository = MergeEntityRepository(listOf())
        val resource = repository.findOne(johnSmithId)
        assertEquals("http://source/JohnSmith", resource.uri)
        assertTrue(resource.model.isEmpty)
    }

    @Test
    fun `if no data is found, returns an empty model`() {
        val repository = MergeEntityRepository(listOf(emptyRepository))
        val resource = repository.findOne(johnSmithId)
        assertEquals("http://source/JohnSmith", resource.uri)
        assertTrue(resource.model.isEmpty)
    }

    @Test
    fun `returns information about an entity`() {
        val repository = MergeEntityRepository(listOf(johnRepository))
        val resource = repository.findOne(johnSmithId)
        assertEquals("http://source/JohnSmith", resource.uri)
        val query = """
            PREFIX src: <http://source/>
            PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#>
            ASK {
                src:JohnSmith vcard:FN "John Smith"
            }
        """.trimIndent()
        assertTrue(resource.model.ask(query))
    }

    @Test
    fun `informacion about an entity in two repositories is merged`() {
        val repository = MergeEntityRepository(listOf(johnRepository, maryRepository))
        val resource = repository.findOne(johnSmithId)
        assertEquals("http://source/JohnSmith", resource.uri)
        val query = """
            PREFIX src: <http://source/>
            PREFIX dc: <http://purl.org/dc/elements/1.1/>
            PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#>
            ASK {
                src:JohnSmith dc:relation ?x .
                ?x vcard:FN "Mary Smith"
            }
        """.trimIndent()
        assertTrue(resource.model.ask(query))
    }
}
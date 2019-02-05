package es.iaaa.kubby.repository.source

import es.iaaa.kubby.repository.EntityId
import es.iaaa.kubby.repository.source.RepositoryMode.CONNECT
import es.iaaa.kubby.repository.source.RepositoryMode.CREATE
import org.apache.jena.dboe.base.file.Location.create
import org.apache.jena.query.Dataset
import org.apache.jena.riot.RDFDataMgr.read
import org.apache.jena.system.Txn.calculateRead
import org.apache.jena.system.Txn.executeWrite
import org.apache.jena.tdb2.TDB2Factory.connectDataset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.*

class Tdb2EntityRepositoryTest {

    private val target: Path = Paths.get("build/datasource/tdb2")
    private val data: Path = Paths.get("src/test/resources/Tetris.n3")
    private val assembler: Path = Paths.get("src/test/resources/tdb2.ttl")

    @BeforeTest
    fun before() {
        target.deleteRecursivelyIfExists()
    }

    @Test
    fun `create an empty new TDB2 dataset by default`() {

        Tdb2EntityRepository(target, data, CREATE)

        assertTrue { Files.isDirectory(target) }
    }

    @Test
    fun `create an empty new TDB2 dataset with an assembler`() {

        Tdb2EntityRepository(assembler, null, CREATE)

        assertTrue { Files.isDirectory(target) }
    }

    @Test
    fun `create and load a new TDB2 dataset`() {

        val ds = Tdb2EntityRepository(target, data, CREATE)
        val result = ds.findOne(EntityId("http://dbpedia.org/resource/", "Tetris"))

        assertFalse { result.model.isEmpty }
    }

    @Test
    fun `connect to an existing TDB2 dataset`() {
        val existing = connectDataset(create(target))
        loadFile(existing)
        val statements = countStatements(existing)

        Tdb2EntityRepository(target, null, CONNECT)

        assertEquals(statements, countStatements(existing))
    }

    @Test
    fun `describe an existing resource in the TB2 dataset`() {
        initializeDataset()

        val ds = Tdb2EntityRepository(target, null, CONNECT)

        val result = ds.findOne(EntityId("http://dbpedia.org/resource/", "Tetris"))

        assertFalse { result.model.isEmpty }
    }

    @Test
    fun `describe a non existing resource in the TB2 dataset`() {
        initializeDataset()

        val ds = Tdb2EntityRepository(target, null, CONNECT)

        val result = ds.findOne(EntityId("http://example.com/resource/", "Tetris"))

        assertTrue { result.model.isEmpty }
    }


    private fun initializeDataset() = connectDataset(create(target)).also { loadFile(it) }

    private fun countStatements(existing: Dataset) = calculateRead(existing) {
        existing.defaultModel.listStatements().toList().size
    }

    private fun loadFile(existing: Dataset) = executeWrite(existing) {
        read(existing, data.toString())
    }


}
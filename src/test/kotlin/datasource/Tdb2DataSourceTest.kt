package es.iaaa.kubby.datasource

import org.apache.jena.dboe.base.file.Location.create
import org.apache.jena.query.Dataset
import org.apache.jena.riot.RDFDataMgr.read
import org.apache.jena.system.Txn.calculateRead
import org.apache.jena.system.Txn.executeWrite
import org.apache.jena.tdb2.TDB2Factory.connectDataset
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Tdb2DataSourceTest {

    private val target: Path = Paths.get("build/datasource/tdb2")
    private val data: Path = Paths.get("src/test/resources/Tetris.n3")
    private val assembler: Path = Paths.get("src/test/resources/tdb2.ttl")


    @Before
    fun before() {
        deleteRecursivelyIfExists(target)
    }

    @Test
    fun `Create an empty new TDB2 dataset by default`() {

        val config = Tdb2DataSourceConfiguration(target)
        val ds = Tdb2DataSource(config)
        ds.init()

        assertTrue { Files.isDirectory(target) }
    }

    @Test
    fun `Create an empty new TDB2 dataset with an assembler`() {

        val config = Tdb2DataSourceConfiguration(assembler)
        val ds = Tdb2DataSource(config)
        ds.init()

        assertTrue { Files.isDirectory(target) }
    }

    @Test
    fun `Create and load a new TDB2 dataset`() {

        val config = Tdb2DataSourceConfiguration(
            path = target,
            definition = DatasourceDefinition.CREATE,
            data = data
        )
        val ds = Tdb2DataSource(config)
        ds.init()
        val result = ds.describe("http://dbpedia.org/resource/Tetris")

        assertFalse { result.isEmpty }
    }


    @Test
    fun `Connect to an existing TDB2 dataset`() {
        val existing = connectDataset(create(target))
        loadFile(existing)
        val statements = countStatements(existing)

        val config = Tdb2DataSourceConfiguration(target)
        val ds = Tdb2DataSource(config)
        ds.init()

        assertEquals(statements, countStatements(existing))
    }

    @Test
    fun `Describe an existing resource in the TB2 dataset`() {
        initializeDataset()

        val config = Tdb2DataSourceConfiguration(target)
        val ds = Tdb2DataSource(config)
        ds.init()

        val result = ds.describe("http://dbpedia.org/resource/Tetris")

        assertFalse { result.isEmpty }
    }

    @Test
    fun `Describe a non existing resource in the TB2 dataset`() {
        initializeDataset()

        val config = Tdb2DataSourceConfiguration(target)
        val ds = Tdb2DataSource(config)
        ds.init()

        val result = ds.describe("http://example.com/resource/Tetris")

        assertTrue { result.isEmpty }
    }


    private fun initializeDataset() = connectDataset(create(target)).also { loadFile(it) }

    private fun countStatements(existing: Dataset) = calculateRead(existing) {
        existing.defaultModel.listStatements().toList().size
    }

    private fun loadFile(existing: Dataset) = executeWrite(existing) {
        read(existing, data.toString())
    }


}
package es.iaaa.kubby.datasource

import org.apache.jena.dboe.base.file.Location
import org.apache.jena.query.Dataset
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.system.Txn
import org.apache.jena.tdb2.TDB2Factory
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
        val existing = TDB2Factory.connectDataset(Location.create(target))
        val statements = loadFile(existing)

        val config = Tdb2DataSourceConfiguration(target)
        val ds = Tdb2DataSource(config)
        ds.init()

        Txn.executeRead(existing) {
            assertEquals(statements, existing.defaultModel.listStatements().toList().size)
        }
    }

    @Test
    fun `Describe an existing resource`() {
        initializeDataset()

        val config = Tdb2DataSourceConfiguration(target)
        val ds = Tdb2DataSource(config)
        ds.init()

        val result = ds.describe("http://dbpedia.org/resource/Tetris")

        assertFalse { result.isEmpty }
    }

    @Test
    fun `Describe a non existing resource`() {
        initializeDataset()

        val config = Tdb2DataSourceConfiguration(target)
        val ds = Tdb2DataSource(config)
        ds.init()

        val result = ds.describe("http://example.com/resource/Tetris")

        assertTrue { result.isEmpty }
    }


    private fun initializeDataset(): Dataset {
        val ds = TDB2Factory.connectDataset(Location.create(target))
        loadFile(ds)
        return ds
    }

    private fun loadFile(existing: Dataset): Int {
        var size = 0
        Txn.executeWrite(existing) {
            RDFDataMgr.read(existing, data.toString())
            size = existing.defaultModel.listStatements().toList().size
        }
        return size
    }
}
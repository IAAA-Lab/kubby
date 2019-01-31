package es.iaaa.kubby.repository.source

import es.iaaa.kubby.repository.EntityId
import es.iaaa.kubby.repository.EntityRepository
import es.iaaa.kubby.repository.source.DatasourceMode.CONNECT
import es.iaaa.kubby.repository.source.DatasourceMode.CREATE
import org.apache.jena.dboe.base.file.Location
import org.apache.jena.query.Dataset
import org.apache.jena.query.QueryExecutionFactory.create
import org.apache.jena.query.QueryFactory
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.sparql.ARQConstants
import org.apache.jena.sparql.core.Quad
import org.apache.jena.sparql.core.describe.DescribeHandler
import org.apache.jena.sparql.core.describe.DescribeHandlerFactory
import org.apache.jena.sparql.core.describe.DescribeHandlerRegistry
import org.apache.jena.sparql.util.Context
import org.apache.jena.system.Txn
import org.apache.jena.system.Txn.calculateRead
import org.apache.jena.tdb2.TDB2Factory.assembleDataset
import org.apache.jena.tdb2.TDB2Factory.connectDataset
import org.apache.jena.vocabulary.RDFS
import java.nio.file.Files.*
import java.nio.file.Path

/**
 * Creates or connect to a TDB2 instance.
 *
 * If [mode] is [CREATE] next if [path] is a folder it is deleted and then the TDB2 instance is created in the same
 * folder otherwise [path] must point to an assembler file; next if a [data] file exists it is used to load the graph
 * store.
 * If [mode] is [CONNECT] then if [path] is a folder tries to connect to the store located at,
 * otherwise [path] must point to an assembler file.
 */
class Tdb2EntityRepository(
    private val path: Path,
    private val data: Path? = null,
    mode: DatasourceMode
) :
    EntityRepository {

    init {
        with(DescribeHandlerRegistry.get()) {
            clear()
            add(BackwardForwardDescribeFactory())
        }
    }

    override fun getId(uri: String) = EntityId(localPart = uri)

    override fun findOne(id: EntityId): Resource = calculateRead(dataset) {
        val query = QueryFactory.create("DESCRIBE <${id.uri}>")
        create(query, dataset).use {
            it.execDescribe()
        }.getResource(id.uri)
    }

    override fun close() {
        dataset.close()
    }

    private val dataset: Dataset = when (mode) {
        CREATE -> createDataset()
        CONNECT -> connectDataset()
    }

    private fun connectDataset() = if (isRegularFile(path)) {
        assembleDataset(path.toString())
    } else {
        connectDataset(Location.create(path))
    }

    private fun createDataset(): Dataset {
        if (isDirectory(path)) {
            path.deleteRecursivelyIfExists()
        }
        val newDataset = connectDataset()
        if (data != null && isRegularFile(data)) {
            Txn.executeWrite(newDataset) {
                RDFDataMgr.read(newDataset, data.toString())
            }
        }
        return newDataset
    }
}

enum class DatasourceMode {
    CONNECT, CREATE
}

/**
 * Deletes recursively a directory.
 */
fun Path.deleteRecursivelyIfExists() {
    if (exists(this)) {
        walk(this)
            .sorted(Comparator.reverseOrder())
            .forEach { deleteIfExists(it) }
    }
}

/**
 * Backward forward describe handler factory.
 */
class BackwardForwardDescribeFactory : DescribeHandlerFactory {
    override fun create(): DescribeHandler {
        return BackwardForwardDescribe()
    }
}

/**
 * Backward forward describe handler.
 */
class BackwardForwardDescribe : DescribeHandler {

    private lateinit var dataset: Dataset
    private lateinit var result: Model
    private lateinit var defaultModel: Model
    private lateinit var unionModel: Model

    override fun start(accumulateResultModel: Model, qContext: Context) {
        result = accumulateResultModel
        dataset = qContext.get(ARQConstants.sysCurrentDataset)
        defaultModel = dataset.defaultModel
        unionModel = dataset.getNamedModel(Quad.unionGraph.uri)
    }

    override fun describe(resource: Resource) {
        with(result) {
            add(defaultModel.listStatements(resource, null, null as RDFNode?))
            add(unionModel.listStatements(resource, null, null as RDFNode?))

            add(defaultModel.listStatements(null, null, resource))
            add(unionModel.listStatements(null, null, resource))

            listObjects()
                .andThen<Resource>(listSubjects())
                .filterDrop { node -> node.isLiteral || resource == node }
                .forEachRemaining { node ->
                    add(defaultModel.listStatements(node as Resource, RDFS.label, null as RDFNode?))
                    add(unionModel.listStatements(node, RDFS.label, null as RDFNode?))
                }

        }
    }

    override fun finish() {
        // Empty block
    }
}


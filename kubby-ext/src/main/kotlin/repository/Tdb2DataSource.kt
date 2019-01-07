package es.iaaa.kubby.repository

import es.iaaa.kubby.repository.DatasourceDefinition.CONNECT
import es.iaaa.kubby.repository.DatasourceDefinition.CREATE
import org.apache.jena.dboe.base.file.Location
import org.apache.jena.query.Dataset
import org.apache.jena.query.QueryExecutionFactory.create
import org.apache.jena.query.QueryFactory
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource
import org.apache.jena.riot.RDFDataMgr.read
import org.apache.jena.sparql.ARQConstants
import org.apache.jena.sparql.core.Quad
import org.apache.jena.sparql.core.describe.DescribeHandler
import org.apache.jena.sparql.core.describe.DescribeHandlerFactory
import org.apache.jena.sparql.core.describe.DescribeHandlerRegistry
import org.apache.jena.sparql.util.Context
import org.apache.jena.system.Txn.calculateRead
import org.apache.jena.system.Txn.executeWrite
import org.apache.jena.tdb2.TDB2Factory.assembleDataset
import org.apache.jena.tdb2.TDB2Factory.connectDataset
import org.apache.jena.vocabulary.RDFS
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.Paths

class Tdb2DataSourceConfiguration(
    val path: Path,
    val definition: DatasourceDefinition = CONNECT,
    val data: Path = Paths.get("data.ttl")
)

class Tdb2DataSource(private val config: Tdb2DataSourceConfiguration) : DataSource {

    override fun qname(uri: String) = QName(localPart = uri)

    init {
        with(DescribeHandlerRegistry.get()) {
            clear()
            add(BackwardForwardDescribeFactory())
        }
    }

    private val dataset: Dataset = when (config.definition) {
        CREATE -> createDataset()
        CONNECT -> connectDataset()
    }

    private fun connectDataset() = if (isRegularFile(config.path)) {
        assembleDataset(config.path.toString())
    } else {
        connectDataset(Location.create(config.path))
    }


    private fun createDataset(): Dataset {
        deleteRecursivelyIfExists(config.path)
        val newDataset = connectDataset()
        executeWrite(newDataset) {
            read(newDataset, config.data.toString())
        }
        return newDataset
    }

    override fun describe(qname: QName): Model {
        val query = QueryFactory.create("DESCRIBE <$qname>")
        return calculateRead(dataset) {
            create(query, dataset).use {
                it.execDescribe()
            }
        }
    }

    override fun close() {
        dataset.close()
    }

}

/**
 * Deletes recursively a directory.
 */
fun deleteRecursivelyIfExists(path: Path) {
    if (exists(path)) {
        walk(path)
            .sorted(Comparator.reverseOrder())
            .forEach { it -> deleteIfExists(it) }
    }
}

class BackwardForwardDescribeFactory : DescribeHandlerFactory {
    override fun create(): DescribeHandler {
        return BackwardForwardDescribe()
    }
}

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

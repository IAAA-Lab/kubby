package es.iaaa.kubby.datasource

import org.apache.jena.dboe.base.file.Location
import org.apache.jena.query.Dataset
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.QueryFactory
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
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
import org.apache.jena.tdb2.TDB2Factory.assembleDataset
import org.apache.jena.tdb2.TDB2Factory.connectDataset
import org.apache.jena.vocabulary.RDFS
import java.nio.file.Files
import java.nio.file.Files.isRegularFile
import java.nio.file.Path
import java.nio.file.Paths

class Tdb2DataSourceConfiguration(
    val path: Path,
    val definition: DatasourceDefinition = DatasourceDefinition.CONNECT,
    val data: Path = Paths.get("data.ttl")
)

class Tdb2DataSource(private val config: Tdb2DataSourceConfiguration) : DataSource {

    private var dataset: Dataset? = null

    override fun init() {
        with(DescribeHandlerRegistry.get()) {
            clear()
            add(BackwardForwardDescribeFactory())
        }
        dataset = when (config.definition) {
            DatasourceDefinition.CREATE -> createDataset()
            DatasourceDefinition.CONNECT -> connectDataset()
        }
    }

    private fun connectDataset() = if (isRegularFile(config.path)) {
        assembleDataset(config.path.toString())
    } else {
        connectDataset(Location.create(config.path))
    }


    private fun createDataset(): Dataset {
        deleteRecursivelyIfExists(config.path)
        val newDataset = connectDataset()
        Txn.executeWrite(newDataset) {
            RDFDataMgr.read(newDataset, config.data.toString())
        }
        return newDataset
    }

    override fun describe(iri: String): Model {
        val query = QueryFactory.create("DESCRIBE <$iri>")
        var model: Model? = null
        Txn.executeRead(dataset) {
            QueryExecutionFactory.create(query, dataset).use {
                model = it.execDescribe()
            }
        }
        return model ?: ModelFactory.createDefaultModel()
    }

    override fun close() {
        dataset?.close()
    }

}

/**
 * Deletes recursively a directory.
 */
fun deleteRecursivelyIfExists(path: Path) {
    if (Files.exists(path)) {
        Files.walk(path)
            .sorted(Comparator.reverseOrder())
            .forEach { it -> Files.deleteIfExists(it) }
    }
}

class BackwardForwardDescribeFactory : DescribeHandlerFactory {
    override fun create(): DescribeHandler {
        return BackwardForwardDescribe()
    }
}

class BackwardForwardDescribe : DescribeHandler {

    private var dataset: Dataset? = null
    private var result: Model? = null
    private var defaultModel: Model? = null
    private var unionModel: Model? = null

    override fun start(accumulateResultModel: Model, qContext: Context) {
        result = accumulateResultModel
        dataset = qContext.get(ARQConstants.sysCurrentDataset)
        defaultModel = dataset!!.defaultModel
        unionModel = dataset!!.getNamedModel(Quad.unionGraph.uri)
    }

    override fun describe(resource: Resource) {
        with(result!!) {
            add(defaultModel!!.listStatements(resource, null, null as RDFNode?))
            add(unionModel!!.listStatements(resource, null, null as RDFNode?))

            add(defaultModel!!.listStatements(null, null, resource))
            add(unionModel!!.listStatements(null, null, resource))

            listObjects()
                .andThen<Resource>(listSubjects())
                .filterDrop { node -> node.isLiteral || resource == node }
                .forEachRemaining { node ->
                    add(defaultModel!!.listStatements(node as Resource, RDFS.label, null as RDFNode?))
                    add(unionModel!!.listStatements(node, RDFS.label, null as RDFNode?))
                }

        }
    }

    override fun finish() {
        // Empty block
    }
}

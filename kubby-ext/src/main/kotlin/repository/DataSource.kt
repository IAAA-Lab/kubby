package es.iaaa.kubby.repository

import org.apache.jena.rdf.model.Model
import java.io.Closeable

/**
 * A Facade interface for the DataSources.
 * This allows to provide several implementations.
 */
interface DataSource : Closeable {

    /**
     * Returns a subgraph describing one resource based on a IRI
     */
    fun describe(namespace: String, localId: String): Model
}

enum class DatasourceDefinition {
    CONNECT, CREATE
}


class EmptyDataSource() : DataSource {
    override fun close() {
        TODO("not implemented")
    }

    override fun describe(namespace: String, localId: String): Model {
        TODO("not implemented")
    }
}
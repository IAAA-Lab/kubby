package es.iaaa.kubby.datasource

import org.apache.jena.rdf.model.Model
import java.io.Closeable

/**
 * A Facade interface for the DataSources.
 * This allows to provide several implementations.
 */
interface DataSource : Closeable {
    /**
     * Initializes the source.
     */
    fun init()

    /**
     * Returns a subgraph describing one resource based on a IRI
     */
    fun describe(iri: String): Model
}

enum class DatasourceDefinition {
    CONNECT, CREATE
}


class EmptyDataSource() : DataSource {
    override fun close() {
        TODO("not implemented")
    }

    override fun describe(relativeIRI: String): Model {
        TODO("not implemented")
    }

    override fun init() {
        TODO("not implemented")
    }
}
package es.iaaa.kubby.sources

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
    fun describe(relativeIRI: String): Model
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
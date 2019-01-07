package es.iaaa.kubby.repository

import org.apache.jena.rdf.model.Model
import java.io.Closeable

/**
 * A Facade interface for the DataSources.
 * This allows to provide several implementations.
 */
interface DataSource : Closeable {

    /**
     * Generate the qname representation for a String in this data source.
     */
    fun qname(uri: String): QName


    /**
     * Get the subgraph describing one resource based on the [qname]
     */
    fun describe(qname: QName): Model
}

enum class DatasourceDefinition {
    CONNECT, CREATE
}

/**
 * Namespace URI to use to represent that there is no Namespace.
 */
const val NULL_NS_URI = ""

/**
 * Qualified name.
 *
 * @param namespaceURI Namespace URI of the QName
 * @param localPart Local part of the QName
 */
data class QName(
    val namespaceURI: String = NULL_NS_URI,
    val localPart: String
) {
    override fun toString() = if (namespaceURI == NULL_NS_URI) localPart else "$namespaceURI$localPart"
}

class EmptyDataSource() : DataSource {
    override fun qname(uri: String): QName {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun describe(qname: QName): Model {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun close() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
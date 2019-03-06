package es.iaaa.kubby.repository.source

import es.iaaa.kubby.domain.Entity
import es.iaaa.kubby.domain.impl.ResourceEntityImpl
import es.iaaa.kubby.repository.EntityRepository
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.apache.jena.query.QueryExecutionFactory.sparqlService
import org.apache.jena.query.QueryFactory

/**
 * Access to a remote SPARQL repository at [service].
 *
 * The RDF Dataset to be queried may be specified in [dataset].
 * Endpoints with self signed certificates and other issues may be trusted by setting [forceTrust] to true.
 */
class SparqlEntityRepository(
    val service: String,
    val dataset: String? = null,
    val forceTrust: Boolean = false,
    val attribution: String? = null
) : EntityRepository {

    override fun localId(uri: String) =  uri

    override fun findOne(namespace: String, localId: String): Entity {
        val uri = "$namespace$localId"
        val resource = sparqlService(
            service,
            QueryFactory.create("DESCRIBE <$uri>"),
            dataset,
            buildClient(),
            null
        ).execDescribe()
        return ResourceEntityImpl(uri = uri, model = resource, attribution = attribution)
    }

    private fun buildClient() = if (forceTrust) {
        val sslContext = SSLContexts.custom().loadTrustMaterial { _, _ -> true }.build()
        val sslConnectionSocketFactory = SSLConnectionSocketFactory(
            sslContext, arrayOf("SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"), null,
            NoopHostnameVerifier.INSTANCE
        )
        HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build()
    } else {
        null
    }

    override fun close() {
        // empty
    }
}


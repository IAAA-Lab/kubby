package es.iaaa.kubby.repository.source

import es.iaaa.kubby.repository.Entity
import es.iaaa.kubby.repository.EntityId
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
    attribution: String? = null
) : EntityRepository {

    val attributionList = attribution?.let{ listOf(it) } ?: emptyList()

    override fun getId(uri: String) = EntityId(localPart = uri)

    override fun findOne(id: EntityId): Entity {
        val resource =        sparqlService(
            service,
            QueryFactory.create("DESCRIBE <${id.uri}>"),
            dataset,
            buildClient(),
            null
        ).execDescribe().getResource(id.uri)
        return Entity(resource = resource, attribution = attributionList).normalize()
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


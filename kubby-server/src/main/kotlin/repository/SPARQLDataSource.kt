package es.iaaa.kubby.repository

import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.QueryFactory
import org.apache.jena.rdf.model.Model


class SPARQLDataSource(
    private val service: String,
    private val defaultGraphURI: String? = null,
    private val forceTrust: Boolean = false,
    private val removeNsPrefix: Regex = "^ns[0-9]+$".toRegex()
) : DataSource {

    override fun describe(namespace: String, localId: String): Model {
        val query = QueryFactory.create("DESCRIBE <$namespace$localId>")
        val client = buildClient()
        val exec = QueryExecutionFactory.sparqlService(service, query, defaultGraphURI, client, null)
        val model = exec.execDescribe()
        model.nsPrefixMap.values
            .asSequence()
            .filter { removeNsPrefix.matches(it) }
            .forEach { model.removeNsPrefix(it) }
        return model
    }

    private fun buildClient() = if (forceTrust) {
        val sslContext = SSLContexts.custom()
            .loadTrustMaterial { _, _ -> true }.build()

        val sslConnectionSocketFactory = SSLConnectionSocketFactory(
            sslContext, arrayOf("SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"), null,
            NoopHostnameVerifier.INSTANCE
        )

        HttpClients.custom()
            .setSSLSocketFactory(sslConnectionSocketFactory)
            .build()

    } else {
        null
    }

    override fun close() {
        // empty
    }
}
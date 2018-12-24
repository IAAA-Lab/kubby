package es.iaaa.kubby.features

import com.github.jsonldjava.core.JsonLdOptions
import io.ktor.application.ApplicationCall
import io.ktor.features.ContentConverter
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.request.ApplicationReceiveRequest
import io.ktor.util.pipeline.PipelineContext
import org.apache.jena.query.DatasetFactory
import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.JsonLDWriteContext
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.RDFFormat
import org.apache.jena.riot.system.PrefixMap
import org.apache.jena.riot.system.RiotLib
import java.io.StringWriter
import java.util.*
import java.util.stream.Collectors.joining


class RiotConverter(private val config: RiotConverterConfiguration) : ContentConverter {
    override suspend fun convertForReceive(context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any? {
        return null
    }

    override suspend fun convertForSend(
        context: PipelineContext<Any, ApplicationCall>,
        contentType: ContentType,
        value: Any
    ): Any? {
        return when (value) {
            is Model -> convertModelForSend(value)
            else -> null
        }
    }

    private fun convertModelForSend(value: Model): String {
        val g = DatasetFactory.wrap(value).asDatasetGraph()
        val pm = RiotLib.prefixMap(g)
        val cxt = JsonLDWriteContext()
        cxt.setJsonLDContext(JsonLDContext(pm).json())
        cxt.setOptions(config.options)
        val writer = RDFDataMgr.createDatasetWriter(config.format)
        val out = StringWriter()
        writer.write(out, g, pm, null, cxt)
        return out.toString()
    }
}

class RiotConverterConfiguration {
    var format = RDFFormat.JSONLD!!
    val options = JsonLdOptions()
    fun options(block: JsonLdOptions.() -> Unit) {
        options.apply(block)
    }
}

/**
 * Register Riot converter into [ContentNegotiation] feature
 */
fun ContentNegotiation.Configuration.riot(
    contentType: ContentType = Rdf.JsonLd,
    block: RiotConverterConfiguration.() -> Unit = {}
) {
    val config = RiotConverterConfiguration()
    config.apply(block)
    val converter = RiotConverter(config)
    register(contentType, converter)
}

object Rdf {
    val JsonLd = ContentType("application", "ld+json")
}

class JsonLDContext(prefixes: PrefixMap) {

    private val cxt = TreeMap<String, String>()

    init {
        putPrefixes(prefixes)
    }

    private fun putPrefixes(prefixes: PrefixMap): JsonLDContext {
        prefixes.mapping.forEach { (key, value) -> putPrefix(key, value.toASCIIString()) }
        return this
    }

    private fun putPrefix(key: String, value: String): JsonLDContext {
        cxt[if (key.isEmpty()) "@vocab" else key] = quote(value)
        return this
    }

    fun json(): String {
        return cxt.entries.stream()
            .map { entry -> quote(entry.key) + ": " + entry.value }
            .collect(joining(", ", "{", "}"))
    }

    companion object {
        private fun quote(s: String): String {
            return '"' + s + '"'
        }
    }
}
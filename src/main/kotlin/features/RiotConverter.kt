package es.iaaa.kubby.features

import com.github.jsonldjava.core.JsonLdOptions
import io.ktor.application.ApplicationCall
import io.ktor.application.call
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

/**
 * Implements output capabilities for common formats (those supported in Pubby and JSON-LD).
 * It adds a Vary header set to Accept when it is configured to convert more than one format
 * or it is required by the configuration.
 *
 * Note #1: The ContentNegotiation feature select converters that match specified Accept header,
 * in order of quality, and then pick the first one that can convert the subject successfully
 * otherwise it will return a 406 NotAcceptable response.
 *
 * Note #2: The ContentNegotiation feature does not add a Vary header when multiple formats are
 * supported.
 */
class RiotConverter(private val config: RiotConverterConfiguration) : ContentConverter {

    override suspend fun convertForReceive(context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any? {
        return null
    }

    override suspend fun convertForSend(
        context: PipelineContext<Any, ApplicationCall>,
        contentType: ContentType,
        value: Any
    ): Any? {
        return if (value is Model && config.contentTypes.contains(contentType)) {
            when (contentType) {
                RDF.JSON_LD -> writeJsonLD(value)
                RDF.TURTLE -> writeTurtle(value)
                RDF.N_TRIPLES -> writeNTriples(value)
                RDF.RDF_XML -> writeRDFXML(value)
                else -> null
            }?.let {
                if (isVaryRequired) {
                    context.call.response.headers.append("Vary", "Accept")
                }
            }
        } else {
            null
        }
    }

    private val isVaryRequired : Boolean
        get() = config.contentTypes.size > 1 || config.varyRequired

    private fun writeJsonLD(model: Model): String {
        val g = DatasetFactory.wrap(model).asDatasetGraph()
        val pm = RiotLib.prefixMap(g)
        val cxt = JsonLDWriteContext()
        cxt.setJsonLDContext(JsonLDContext(pm).json())
        cxt.setOptions(config.options)
        val writer = RDFDataMgr.createDatasetWriter(config.jsonldFormatVariant)
        val out = StringWriter()
        writer.write(out, g, pm, null, cxt)
        return out.toString()
    }

    private fun writeTurtle(model: Model): String {
        val out = StringWriter()
        RDFDataMgr.write(out, model, config.turtleFormatVariant)
        return out.toString()
    }

    private fun writeNTriples(model: Model): String {
        val out = StringWriter()
        RDFDataMgr.write(out, model, config.ntriplesFormatVariant)
        return out.toString()
    }

    private fun writeRDFXML(model: Model): String {
        val out = StringWriter()
        RDFDataMgr.write(out, model, config.rdfxmlFormatVariant)
        return out.toString()
    }

}

class RiotConverterConfiguration {
    val contentTypes =  mutableSetOf(RDF.JSON_LD)
    var jsonldFormatVariant : RDFFormat = RDFFormat.JSONLD
    var turtleFormatVariant : RDFFormat = RDFFormat.TURTLE
    var ntriplesFormatVariant : RDFFormat = RDFFormat.NTRIPLES
    var rdfxmlFormatVariant : RDFFormat = RDFFormat.RDFXML
    var varyRequired : Boolean = false
    val options = JsonLdOptions()
    fun options(block: JsonLdOptions.() -> Unit) {
        options.apply(block)
    }
}

/**
 * Register Riot converter into [ContentNegotiation] feature
 */
fun ContentNegotiation.Configuration.riot(
    block: RiotConverterConfiguration.() -> Unit = {}
) {
    val config = RiotConverterConfiguration()
    config.apply(block)
    val converter = RiotConverter(config)
    config.contentTypes.forEach { register(it, converter) }
}

object RDF {
    val JSON_LD = ContentType("application", "ld+json")
    val TURTLE = ContentType("text", "turtle")
    val RDF_XML = ContentType("application", "rdf+xml")
    val N_TRIPLES = ContentType("application", "n-triples")
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
package es.iaaa.kubby.ktor.features

import com.github.jsonldjava.core.JsonLdOptions
import es.iaaa.kubby.domain.Entity
import es.iaaa.kubby.rdf.JsonLDContext
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
import org.apache.jena.riot.system.RiotLib
import java.io.StringWriter

/**
 * Implements output capabilities for common formats (those supported in Pubby and JSON-LD).
 * It adds a Vary header set to Accept when it is configured to convert more than one format
 * or it is required by the configuration.
 *
 * Notes:
 * * The ContentNegotiation feature select converters that match specified Accept header,
 * in order of quality, and then pick the first one that can convert the subject successfully
 * otherwise it will return a 406 NotAcceptable response.
 * * The ContentNegotiation feature does not add a Vary header when multiple formats are
 * supported.
 */
class RdfConverter(private val config: RdfConverterConfiguration) : ContentConverter {

    override suspend fun convertForReceive(context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any? {
        return null
    }

    override suspend fun convertForSend(
        context: PipelineContext<Any, ApplicationCall>,
        contentType: ContentType,
        value: Any
    ): Any? = if (value is Entity && config.contentTypes.contains(contentType)) {
        val graph = value.toGraphModel() as? Model
        graph?.let {
            if (isVaryRequired) {
                context.call.response.headers.append("Vary", "Accept")
            }
            graph.run {
                when (contentType) {
                    RDF.TURTLE -> toString(config.turtleFormatVariant)
                    RDF.N_TRIPLES -> toString(config.ntriplesFormatVariant)
                    RDF.RDF_XML -> toString(config.rdfxmlFormatVariant)
                    else -> toString(config.jsonldFormatVariant, config.options)
                }
            }
        }
    } else {
        null
    }

    private val isVaryRequired: Boolean
        get() = config.contentTypes.size > 1 || config.varyRequired
}

/**
 * Converter configuration.
 */
class RdfConverterConfiguration {
    val contentTypes = mutableSetOf(RDF.JSON_LD)
    var jsonldFormatVariant: RDFFormat = RDFFormat.JSONLD
    var turtleFormatVariant: RDFFormat = RDFFormat.TURTLE
    var ntriplesFormatVariant: RDFFormat = RDFFormat.NTRIPLES
    var rdfxmlFormatVariant: RDFFormat = RDFFormat.RDFXML
    var varyRequired: Boolean = false
    val options = JsonLdOptions()
    fun options(block: JsonLdOptions.() -> Unit) {
        options.apply(block)
    }
}

/**
 * Known RDF content types.
 */
object RDF {
    val JSON_LD = ContentType("application", "ld+json")
    val TURTLE = ContentType("text", "turtle")
    val RDF_XML = ContentType("application", "rdf+xml")
    val N_TRIPLES = ContentType("application", "n-triples")
}

/**
 * Register Riot converter into [ContentNegotiation] feature
 */
fun ContentNegotiation.Configuration.rdf(
    block: RdfConverterConfiguration.() -> Unit = {}
) {
    val config = RdfConverterConfiguration().apply(block)
    val converter = RdfConverter(config)
    config.contentTypes.forEach { register(it, converter) }
}

/**
 * Serializes with [format].
 */
fun Model.toString(format: RDFFormat): String {
    val out = StringWriter()
    RDFDataMgr.write(out, this, format)
    return out.toString()
}

/**
 * Serializes with a [variant] of JSON-LD according to the [options].
 */
fun Model.toString(variant: RDFFormat, options: JsonLdOptions): String {
    val g = DatasetFactory.wrap(this).asDatasetGraph()
    val pm = RiotLib.prefixMap(g)
    val cxt = JsonLDWriteContext().apply {
        setJsonLDContext(JsonLDContext(pm).toJson())
        setOptions(options)
    }
    val writer = RDFDataMgr.createDatasetWriter(variant)
    val out = StringWriter()
    writer.write(out, g, pm, null, cxt)
    return out.toString()
}

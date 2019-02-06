package es.iaaa.kubby.ktor.features

import es.iaaa.kubby.config.ProjectDescription
import es.iaaa.kubby.rest.api.ContentContext
import es.iaaa.kubby.rest.api.requestContextKey
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.response.ApplicationSendPipeline
import io.ktor.util.AttributeKey
import org.apache.jena.rdf.model.Model
import org.koin.ktor.ext.inject


/**
 * Metadata feature.
 *
 * It adds to the pipeline [MetadataProcesor] able to enrich [Model]s.
 */
class Metadata(val registrations: List<MetadataProcesor>) {
    class Configuration {
        internal val registrations = mutableListOf<MetadataProcesor>()
        fun <T : MetadataProcesor> register(converter: T, configuration: T.() -> Unit = {}) {
            val registration = converter.apply(configuration)
            registrations.add(registration)
        }

    }

    companion object Feature :
        ApplicationFeature<ApplicationCallPipeline, Configuration, Metadata> {

        override val key = AttributeKey<Metadata>("Metadata")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): Metadata {
            val configuration = Configuration().apply(configure)
            val feature = Metadata(configuration.registrations)

            pipeline.sendPipeline.intercept(ApplicationSendPipeline.Transform) { subject ->
                if (subject is Model) {
                    call.application.apply {
                        val projectDesc by inject<ProjectDescription>()
                        call.attributes.getOrNull(requestContextKey)?.let { ctx ->
                            feature.registrations.forEach { it.process(subject, ctx, projectDesc) }
                        }
                    }
                }
                proceedWith(subject)
            }
            return feature
        }
    }
}

/**
 * The interface of the processors supported by this feature.
 */
interface MetadataProcesor {
    /**
     * Enrich a [model] given a [context] and a [projectDescription].
     */
    fun process(model: Model, context: ContentContext, projectDescription: ProjectDescription)
}
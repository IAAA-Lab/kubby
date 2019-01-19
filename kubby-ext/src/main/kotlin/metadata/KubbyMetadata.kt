package es.iaaa.kubby.metadata

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.response.ApplicationSendPipeline
import io.ktor.util.AttributeKey
import org.apache.jena.rdf.model.Model

class KubbyMetadata(val registrations: List<MetadataAugmenter>) {
    class Configuration {
        internal val registrations = mutableListOf<MetadataAugmenter>()
        fun <T : MetadataAugmenter> register(converter: T, configuration: T.() -> Unit = {}) {
            val registration = converter.apply(configuration)
            registrations.add(registration)
        }

    }

    companion object Feature :
        ApplicationFeature<ApplicationCallPipeline, Configuration, KubbyMetadata> {

        override val key = AttributeKey<KubbyMetadata>("KubbyMetadata")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): KubbyMetadata {
            val configuration = Configuration().apply(configure)
            val feature = KubbyMetadata(configuration.registrations)

            pipeline.sendPipeline.intercept(ApplicationSendPipeline.Transform) { subject ->
                if (subject is Model) {
                    feature.registrations.forEach {
                        it.augment(subject, call.attributes, call.application.environment.config)
                    }
                }
                proceedWith(subject)
            }
            return feature
        }
    }
}
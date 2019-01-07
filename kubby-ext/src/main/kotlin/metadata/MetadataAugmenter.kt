package es.iaaa.kubby.metadata

import io.ktor.config.ApplicationConfig
import io.ktor.util.Attributes
import org.apache.jena.rdf.model.Model

interface MetadataAugmenter {
    fun augment(model: Model, attributes: Attributes, props: ApplicationConfig)
}
package es.iaaa.kubby.server.metadata

import io.ktor.util.Attributes
import org.apache.jena.rdf.model.Model

interface MetadataAugmenter {
    fun augment(model: Model, attributes: Attributes)
}
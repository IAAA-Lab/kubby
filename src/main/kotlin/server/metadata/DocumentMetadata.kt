package es.iaaa.kubby.server.metadata

import es.iaaa.kubby.datasource.addNsIfUndefined
import es.iaaa.kubby.util.AttributeKeys.pageId
import es.iaaa.kubby.util.AttributeKeys.resourceId
import io.ktor.util.Attributes
import org.apache.jena.rdf.model.Model
import org.apache.jena.sparql.vocabulary.FOAF

class DocumentMetadata : MetadataAugmenter {
    override fun augment(model: Model, attributes: Attributes) {
        val resourceId = attributes.getOrNull(resourceId)
        val pageId = attributes.getOrNull(pageId)

        if (resourceId != null && pageId != null) {
            val topic = model.getResource(resourceId)
            val document = model.getResource(pageId)
            document.addProperty(FOAF.primaryTopic, topic)
            model.addNsIfUndefined("foaf", FOAF.getURI())
        }
    }
}

fun Metadata.Configuration.documentMetadata() {
    register(DocumentMetadata())
}


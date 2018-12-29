package es.iaaa.kubby.server.metadata

import es.iaaa.kubby.config.Configuration.config
import es.iaaa.kubby.config.Configuration.defaultLocale
import es.iaaa.kubby.datasource.addNsIfUndefined
import es.iaaa.kubby.util.AttributeKeys.pageId
import es.iaaa.kubby.util.AttributeKeys.resourceId
import es.iaaa.kubby.util.getTitle
import io.ktor.util.Attributes
import org.apache.jena.rdf.model.Model
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.RDFS

class DocumentMetadata : MetadataAugmenter {
    override fun augment(model: Model, attributes: Attributes) {
        val resourceId = attributes.getOrNull(resourceId)
        val pageId = attributes.getOrNull(pageId)

        if (resourceId != null && pageId != null) {
            val topic = model.getResource(resourceId)
            val document = model.getResource(pageId)
            val title = topic.getTitle(defaultLocale) ?: anonTitle

            model.addNsIfUndefined("foaf", FOAF.getURI())
            model.addNsIfUndefined("rdfs", RDFS.getURI())

            document.addProperty(FOAF.primaryTopic, topic)
            document.addProperty(RDFS.label, documentLabel.format(title))
        }
    }

    companion object {
        val base = "kubby.locale-data.$defaultLocale.resources.metadata"
        val anonTitle = config.getString("$base.document-label-anon")
        val documentLabel = config.getString("$base.document-label")
    }

}


fun Metadata.Configuration.documentMetadata() {
    register(DocumentMetadata())
}


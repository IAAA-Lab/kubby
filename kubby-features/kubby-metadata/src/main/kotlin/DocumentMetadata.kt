package es.iaaa.kubby.metadata

import com.typesafe.config.Config
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

    private val base: Config = config.getConfig("kubby.locale-data.$defaultLocale.resources.metadata")
    private val anonTitle: String = base.getString("document-label-anon")
    private val documentLabel: String = base.getString("document-label")

    override fun augment(model: Model, attributes: Attributes) {
        val resourceId = attributes.getOrNull(resourceId)
        val pageId = attributes.getOrNull(pageId)

        if (resourceId != null && pageId != null) {
            val topic = model.getResource(resourceId)
            val document = model.getResource(pageId)
            val title = topic.getTitle(defaultLocale) ?: anonTitle

            model.addNsIfUndefined("foaf", FOAF.NS)
            model.addNsIfUndefined("rdfs", RDFS.uri)

            document.addProperty(FOAF.primaryTopic, topic)
            document.addProperty(RDFS.label, documentLabel.format(title))
        }
    }
}


fun Metadata.Configuration.documentMetadata() {
    register(DocumentMetadata())
}


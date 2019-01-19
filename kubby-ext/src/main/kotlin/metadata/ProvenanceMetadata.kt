package es.iaaa.kubby.metadata

import es.iaaa.kubby.config.softwareName
import es.iaaa.kubby.content.ContentKeys
import es.iaaa.kubby.content.ContentKeys.pageId
import es.iaaa.kubby.model.addNsIfUndefined
import io.ktor.config.ApplicationConfig
import io.ktor.util.Attributes
import org.apache.jena.rdf.model.Model
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.RDF
import java.util.*

class ProvenanceMetadata : MetadataAugmenter {

    override fun augment(model: Model, attributes: Attributes, props: ApplicationConfig) {
        val pageId = attributes.getOrNull(pageId)

        if (pageId != null) {
            val aboutId: String = attributes[ContentKeys.aboutId]
            val time: Calendar = attributes[ContentKeys.timeId]
            val date = model.createTypedLiteral(time)


            val document = model.getResource(pageId)
            val agent = model.getResource(aboutId)
            val prov = "http://www.w3.org/ns/prov#"

            model.addNsIfUndefined("prov", prov)
            model.addNsIfUndefined("rdf", RDF.uri)
            model.addNsIfUndefined("foaf", FOAF.NS)

            document.addProperty(RDF.type, model.createResource(prov + "Entity"))
            document.addLiteral(model.createProperty(prov, "generatedAtTime"), date)

            val activity = model.createResource()
            model.add(activity, RDF.type, model.createResource(prov + "Activity"))
            activity.addProperty(model.createProperty(prov, "generated"), document)
            activity.addProperty(model.createProperty(prov, "wasAssociatedWith"), agent)
            document.addProperty(model.createProperty(prov, "wasGeneratedBy"), activity)

            agent.addProperty(RDF.type, model.createResource(prov + "SoftwareAgent"))
            agent.addProperty(RDF.type, model.createResource(prov + "Agent"))
            agent.addLiteral(FOAF.name, props.softwareName)
        }
    }
}


fun KubbyMetadata.Configuration.provenanceMetadata() {
    register(ProvenanceMetadata())
}


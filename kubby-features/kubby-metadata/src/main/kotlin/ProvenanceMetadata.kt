package es.iaaa.kubby.metadata

import com.typesafe.config.Config
import es.iaaa.kubby.config.Configuration
import es.iaaa.kubby.datasource.addNsIfUndefined
import es.iaaa.kubby.util.AttributeKeys
import es.iaaa.kubby.util.AttributeKeys.pageId
import io.ktor.util.Attributes
import org.apache.jena.rdf.model.Model
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.RDF
import java.util.*

class ProvenanceMetadata : MetadataAugmenter {

    private val config: Config = Configuration.config.getConfig("kubby")
    private val name: String = config.getString("name")

    override fun augment(model: Model, attributes: Attributes) {
        val pageId = attributes.getOrNull(pageId)

        if (pageId != null) {
            val aboutId: String = attributes[AttributeKeys.aboutId]
            val time: Calendar = attributes[AttributeKeys.timeId]
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
            agent.addLiteral(FOAF.name, name)
        }
    }
}


fun Metadata.Configuration.provenanceMetadata() {
    register(ProvenanceMetadata())
}


package es.iaaa.kubby.domain.impl

import es.iaaa.kubby.domain.Entity
import es.iaaa.kubby.domain.IncompatibleEntityException
import es.iaaa.kubby.rdf.addNsIfUndefined
import es.iaaa.kubby.rdf.rewrite
import es.iaaa.kubby.text.replacePrefix
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory

data class ResourceEntityImpl(
    override val uri: String,
    val model: Model,
    val prefixes: Map<String, String> = emptyMap(),
    val attribution: String? = null
) : Entity {

    override fun rewrite(old: String, new: String): Entity {
        val newUri = uri.replacePrefix(old, new)
        return copy(uri = newUri, model = model.rewrite(old, new))
    }

    override fun toGraphModel() = ModelFactory.createDefaultModel().add(model).addNsIfUndefined(prefixes)
    override val attributions: List<String>
        get() = if (isEmpty || attribution.isNullOrBlank()) emptyList() else listOf(
            attribution
        )
    override val isEmpty: Boolean get() = model.isEmpty
    override fun merge(other: Entity): Entity {
        if (uri != other.uri) throw IncompatibleEntityException("Other uri ${other.uri} is different from $uri")
        return if (other.isEmpty) this else MergedEntityImpl(uri, listOf(this, other))
    }

    override fun merge(other: Map<String, String>) = copy(prefixes = prefixes.plus(other))
    override fun addSameAs(other: String): Entity = merge(
        sameAsGranule(
            uri,
            other
        )
    )
}
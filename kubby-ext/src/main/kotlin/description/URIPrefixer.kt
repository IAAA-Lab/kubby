package description

import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.shared.PrefixMapping

private val pattern = "([^#/:?]+)[#/:?]*$".toRegex()

/**
 * Helper class that splits URIs into _prefix and local name
 * according to a Jena PrefixMapping.
 */
class URIPrefixer(private val resource: Resource, prefixes: PrefixMapping) {
    private var _prefix: String? = null
    private var _localName: String? = null

    constructor(uri: String, prefixes: PrefixMapping) : this(ResourceFactory.createResource(uri), prefixes)

    init {
        resource.uri?.let { uri ->
            prefixes.nsPrefixMap.toList()
                .firstOrNull { (_, ns) -> uri.startsWith(ns) }
                ?.let { (p, ns) ->
                    _prefix = p
                    _localName = uri.substring(ns.length)
                }
        }
    }

    val prefix: String?
        get() = _prefix

    val hasPrefix: Boolean
        get() = _prefix != null

    val localName: String?
        get() {
            if (resource.isAnon) return null
            return _localName ?: pattern.find(resource.uri)?.let { it.groupValues[0] } ?: ""
        }

    fun toTurtle(): String {
        if (resource.isAnon) return "[]"
        return if (hasPrefix) "$_prefix:$localName" else "<${resource.uri}>"
    }
}

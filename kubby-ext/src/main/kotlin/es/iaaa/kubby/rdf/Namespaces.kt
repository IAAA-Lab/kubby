package es.iaaa.kubby.rdf

import org.apache.jena.shared.PrefixMapping


/**
 * Merges the [PrefixMapping] with [other] mapping.
 */
fun PrefixMapping.mergePrefixes(other: Map<String, String>) =
    other.forEach { prefix, uri -> setNsPrefix(prefix, uri) }

/**
 * Retrieves the uris of the [PrefixMapping].
 */
fun PrefixMapping.uris() = nsPrefixMap.values.toList()

/**
 * Remove prefixes that starts with a pattern.
 */
@JvmOverloads
fun PrefixMapping.prunePrefixes(regex: Regex = "ns[1-9]\\d*".toRegex()) =
    nsPrefixMap.keys.filter { regex.matches(it) }.forEach { removeNsPrefix(it) }


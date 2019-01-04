package es.iaaa.kubby.util

import es.iaaa.kubby.config.Configuration
import es.iaaa.kubby.config.Configuration.defaultLocale
import es.iaaa.kubby.config.Configuration.labelProperties
import es.iaaa.kubby.config.Configuration.locales
import io.ktor.http.RequestConnectionPoint
import org.apache.jena.rdf.model.*
import org.apache.jena.shared.PrefixMapping
import org.apache.jena.shared.impl.PrefixMappingImpl
import java.util.*

fun RequestConnectionPoint.buildBase(path: String): String {
    val sb = StringBuilder()
    sb.append("$scheme://$host")
    when (scheme) {
        "http" -> if (port != 80) sb.append(":$port")
        "https" -> if (port != 443) sb.append(":$port")
        else -> {
        }
    }
    val keep = uri.length - path.length
    sb.append(uri.subSequence(0, keep))
    return sb.toString()
}


fun RequestConnectionPoint.buildRequest(): String {
    val sb = StringBuilder()
    sb.append("$scheme://$host")
    when (scheme) {
        "http" -> if (port != 80) sb.append(":$port")
        "https" -> if (port != 443) sb.append(":$port")
        else -> {
        }
    }
    sb.append(uri)
    return sb.toString()
}

private val camelCaseBoundaryPattern =
    "(?<=(\\p{javaLowerCase}|\\p{javaUpperCase})\\p{javaLowerCase})(?=\\p{javaUpperCase})".toRegex()

private val wordPattern = "[ \t\r\n-]+".toRegex()

fun String?.toTitleCase(lang: String? = defaultLocale): String? {
    if (this == null) return null

    val uncapitalizedWords = if (locales.containsKey(lang)) {
        locales.getValue(lang!!).uncapitalizedWords
    } else {
        emptySet()
    }

    val str = camelCaseBoundaryPattern.replace(this, " ")
    return if (lang != null) toTitleCase(str, Locale.forLanguageTag(lang), uncapitalizedWords)
    else toTitleCase(str, uncapitalizedWords)
}

private fun toTitleCase(
    str: String,
    locale: Locale,
    uncapitalizedWords: Set<String>
): String {
    return wordPattern.split(str)
        .filter { !it.isEmpty() }
        .map { it.toLowerCase(locale) }
        .fold("") { acc, word ->
            "$acc " + if (acc.isEmpty() || !uncapitalizedWords.contains(word)) {
                word.substring(0, 1).toUpperCase(locale) + word.substring(1)
            } else {
                word
            }
        }
        .trimStart()
}

private fun toTitleCase(
    str: String,
    uncapitalizedWords: Set<String>
): String {
    return wordPattern.split(str)
        .filter { !it.isEmpty() }
        .map { it.toLowerCase() }
        .fold("") { acc, word ->
            "$acc " + if (acc.isEmpty() || !uncapitalizedWords.contains(word)) {
                word.substring(0, 1).toUpperCase() + word.substring(1)
            } else {
                word
            }
        }
        .trimStart()
}

fun Resource.getTitle(lang: String?): String? {
    fun extractTitle(): String? {
        val match = prefixes.nsPrefixMap.toList().find { uri.startsWith(it.second) }
        return if (match != null) uri.substring(match.second.length) else null
    }
    if (!this.isResource) return null
    val literal = getLabel(lang)
    val label = literal?.lexicalForm ?: extractTitle()
    return if ("" == label) uri.toTitleCase(null) else label.toTitleCase(literal?.language)
}

fun Resource.getValuesFromMultipleProperties(properties: Collection<Property>) =
    properties.flatMap {
        this.listProperties(it).toList().map { stmt -> stmt.`object` }
    }

fun getBestLanguageMatch(nodes: Collection<RDFNode>, lang: String?): Literal? {
    val literals = nodes.filter { it.isLiteral }.map { it.asLiteral() }
    return literals.find { lang == null || lang == it.language } ?: literals.firstOrNull()
}

fun Resource.getLabel(lang: String?): Literal? {
    val candidates = getValuesFromMultipleProperties(labelProperties)
    return getBestLanguageMatch(candidates, lang)
}

val Resource.prefixes: PrefixMapping
    get() {
        val prefixes = PrefixMappingImpl()
        prefixes.setNsPrefixes(this.model)
        Configuration.prefixes.forEach { prefix, uri -> prefixes.setNsPrefix(prefix, uri) }
        return prefixes
    }

fun Model.addNsIfUndefined(prefix: String, uri: String) {
    if (this.getNsURIPrefix(uri) != null) return
    if (this.getNsPrefixURI(prefix) != null) return
    this.setNsPrefix(prefix, uri)
}
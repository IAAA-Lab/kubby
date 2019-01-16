package es.iaaa.kubby.util

import es.iaaa.kubby.config.*
import io.ktor.config.ApplicationConfig
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

fun String?.toTitleCase(lang: String?, props: ApplicationConfig): String? {
    if (this == null) return null

    val uncapitalizedWords = props.list("uncapitalized-words", lang ?: props.defaultLanguage)

    val str = camelCaseBoundaryPattern.replace(this, " ")
    return if (lang != null) toTitleCase(str, Locale.forLanguageTag(lang), uncapitalizedWords)
    else toTitleCase(str, uncapitalizedWords)
}

private fun toTitleCase(
    str: String,
    uncapitalizedWords: List<String>
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

private fun toTitleCase(
    str: String,
    locale: Locale,
    uncapitalizedWords: List<String>
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


fun Resource.getTitle(lang: String?, props: ApplicationConfig): String? {
    fun extractTitle(): String? {
        val match = getPrefixes(props).nsPrefixMap.toList().find { uri.startsWith(it.second) }
        return if (match != null) uri.substring(match.second.length) else null
    }
    if (!this.isResource) return null
    val literal = getLabel(lang, props)
    val label = (literal?.lexicalForm ?: extractTitle()).preferNull
        ?: literal?.language.preferNull
    return label.toTitleCase(lang, props)
}


val String?.preferNull: String?
    get() = if (this == null || isBlank()) null else this

fun Resource.getValuesFromMultipleProperties(properties: Collection<Property>) =
    properties.flatMap {
        this.listProperties(it).toList().map { stmt -> stmt.`object` }
    }

fun getBestLanguageMatch(nodes: Collection<RDFNode>, lang: String?): Literal? {
    val literals = nodes.filter { it.isLiteral }.map { it.asLiteral() }
    return literals.find { lang.isNullOrEmpty() || lang == it.language } ?: literals.firstOrNull()
}

fun Resource.getLabel(lang: String?, props: ApplicationConfig): Literal? {
    val candidates = getValuesFromMultipleProperties(props.labelProperties)
    return getBestLanguageMatch(candidates, lang)
}


fun Resource.getComment(lang: String?, props: ApplicationConfig): String? {
    val candidates = getValuesFromMultipleProperties(props.commentProperties)
    return getBestLanguageMatch(candidates, lang)?.let { toSentenceCase(it.lexicalForm, it.language)}
}

fun Resource.getImageURL(props: ApplicationConfig): String? = getValuesFromMultipleProperties(props.imageProperties)
    .firstOrNull { it.isURIResource }?.asResource()?.uri



private fun toSentenceCase(s: String?, lang: String): String? = s?.trim()?.let { if (it == "") null else it.capitalize() }


fun Resource.getPrefixes(props: ApplicationConfig): PrefixMapping {
    val prefixes = PrefixMappingImpl()
    prefixes.setNsPrefixes(this.model)
    props.usePrefixes.forEach { prefix, uri -> prefixes.setNsPrefix(prefix, uri) }
    return prefixes
}

fun Model.addNsIfUndefined(prefix: String, uri: String) {
    if (this.getNsURIPrefix(uri) != null) return
    if (this.getNsPrefixURI(prefix) != null) return
    this.setNsPrefix(prefix, uri)
}
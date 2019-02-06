package es.iaaa.kubby.text

/**
 * This [String] to title case but words in the [except] list.
 */
fun String.toTitleCase(except: List<String> = emptyList()) =
    replace(camelCaseBoundaryPattern, " ").split(wordPatternBoundaries)
        .filter { !it.isEmpty() }
        .map { it.toLowerCase() }
        .fold("") { acc, word ->
            "$acc " + if (acc.isEmpty() || !except.contains(word)) word.capitalize() else word
        }
        .trimStart()


/**
 * Replaces the [old] prefix by the [new] prefix.
 */
fun String.replacePrefix(old: String, new: String) = if (startsWith(old)) new + substring(old.length) else this

/**
 * Camel case boundaries used in [String.toTitleCase].
 */
private val camelCaseBoundaryPattern =
    "(?<=(\\p{javaLowerCase}|\\p{javaUpperCase})\\p{javaLowerCase})(?=\\p{javaUpperCase})".toRegex()

/**
 * Word pattern delimiters used in [String.toTitleCase].
 */
private val wordPatternBoundaries = "[ \t\r\n-]+".toRegex()
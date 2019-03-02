package es.iaaa.kubby

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.ktor.config.tryGetString
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Creates an [Config] instance from command line arguments.
 */
fun commandLineConfig(args: Array<String>): Config {

    val argsMap = args.mapNotNull { it.splitPair('=') }.toMap()

    val configFile = argsMap["-config"]?.let { File(it) }
    val commandLineMap = argsMap.filterKeys { it.startsWith("-P:") }.mapKeys { it.key.removePrefix("-P:") }

    val defaultConfig = ConfigFactory.load()
    val environmentConfig = ConfigFactory.systemProperties().withOnlyPath("ktor")
    val fileConfig = configFile?.let { ConfigFactory.parseFile(it) } ?: ConfigFactory.load()
    val argConfig = ConfigFactory.parseMap(commandLineMap, "Command-line options")
    val combinedConfig = argConfig.withFallback(fileConfig).withFallback(environmentConfig).withFallback(defaultConfig).resolve()

    val applicationIdPath = "ktor.application.id"
    val applicationId = combinedConfig.tryGetString(applicationIdPath) ?: "Application"
    val log = LoggerFactory.getLogger(applicationId)

    log.apply {
        if (combinedConfig.hasPath("kubby"))
            trace(combinedConfig.getObject("kubby").render())
        else
            trace("""
                No configuration provided for Kubby: neither application.conf
                nor system properties nor command line options (-config or -P:kubby...=) provided
            """.trimIndent())
    }
    return combinedConfig
}

private fun String.splitPair(ch: Char): Pair<String, String>? = indexOf(ch).let { idx ->
    when (idx) {
        -1 -> null
        else -> Pair(take(idx), drop(idx + 1))
    }
}

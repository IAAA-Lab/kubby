package es.iaaa.kubby.util

import com.typesafe.config.Config
import es.iaaa.kubby.config.Configuration
import io.ktor.http.RequestConnectionPoint

fun RequestConnectionPoint.buildResourceNamespace(route: String, id: String): String {
    val sb = StringBuilder()
    sb.append("$scheme://$host")
    when (scheme) {
        "http" -> if (port != 80) sb.append(":$port")
        "https" -> if (port != 443) sb.append(":$port")
        else -> {
        }
    }
    val keep = uri.length - route.length - id.length - 1
    sb.append(uri.subSequence(0, keep))
    sb.append(Configuration.route.resource)
    sb.append("/")
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


fun Config.getStringOrDefault(path: String, default: String): String =
    if (hasPath(path)) {
        getString(path)
    } else {
        default
    }

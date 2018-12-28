package es.iaaa.kubby.server

import es.iaaa.kubby.datasource.DataSource
import es.iaaa.kubby.server.routes.data
import es.iaaa.kubby.server.routes.page
import es.iaaa.kubby.server.routes.resource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.Routing

fun Routing.setup(
    dataSource: DataSource
) {
    static("static") {
        resources("static")
    }
    resource()
    data(dataSource)
    page()

}
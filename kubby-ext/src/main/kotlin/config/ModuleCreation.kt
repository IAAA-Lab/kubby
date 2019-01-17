package es.iaaa.kubby.config

import es.iaaa.kubby.repository.DataSource
import es.iaaa.kubby.repository.MergeDataSource
import es.iaaa.kubby.repository.RewrittenDataSource
import es.iaaa.kubby.repository.SPARQLDataSource
import io.ktor.config.ApplicationConfig
import org.koin.dsl.module.Module
import org.koin.dsl.module.module

fun createModule(config: ApplicationConfig): Module {
    val ds = config.datasets.filterIsInstance(SparqlEndpoint::class.java)
        .map {
            RewrittenDataSource(
                dataSource = SPARQLDataSource(
                    service = it.endpoint,
                    defaultGraphURI = it.defaultGraph,
                    forceTrust = it.trustEndpoint
                ),
                target = it.datasetBase
            )
        }
    return module(createOnStart = false) {
        single<DataSource> {
            MergeDataSource(ds)
        }
        single {
            config
        }
    }
}

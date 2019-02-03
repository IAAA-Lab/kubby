package es.iaaa.kubby.config

import es.iaaa.kubby.services.DescribeEntityService
import es.iaaa.kubby.services.IndexService
import es.iaaa.kubby.services.impl.DefaultDescribeEntityServiceImpl
import es.iaaa.kubby.services.impl.DefaultIndexServiceImpl
import io.ktor.config.ApplicationConfig
import org.koin.dsl.module.Module
import org.koin.dsl.module.module

fun createKubbyModule(appConf: ApplicationConfig): Module {
    val conf = appConf.toProjectDescription()
    return module(createOnStart = false) {
        single {
            appConf.toEntityRepository()
        }
        single<IndexService> {
            DefaultIndexServiceImpl(get(), conf.indexResource)
        }
        single<DescribeEntityService> {
            DefaultDescribeEntityServiceImpl(
                entityRepository = get(),
                prefixes = conf.usePrefixes
            )
        }
        single {
            conf
        }
        single {
            appConf.toRoutes()
        }
    }
}





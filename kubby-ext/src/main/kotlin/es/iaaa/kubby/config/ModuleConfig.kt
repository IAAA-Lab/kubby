package es.iaaa.kubby.config

import com.typesafe.config.Config
import es.iaaa.kubby.services.DescribeEntityService
import es.iaaa.kubby.services.IndexService
import es.iaaa.kubby.services.impl.DescribeEntityServiceImpl
import es.iaaa.kubby.services.impl.IndexServiceImpl
import org.koin.dsl.module.Module
import org.koin.dsl.module.module

fun module(config: Config): Module {
    return module(createOnStart = false) {
        single { config.toProjectDescription() }
        single { config.toEntityRepository() }
        single<IndexService> { IndexServiceImpl(get(), get<ProjectDescription>().indexResource) }
        single<DescribeEntityService> { DescribeEntityServiceImpl(get(), get<ProjectDescription>().usePrefixes) }
        single { config.toRoutes() }
    }
}





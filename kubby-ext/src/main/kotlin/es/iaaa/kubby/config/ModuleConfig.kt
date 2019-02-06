package es.iaaa.kubby.config

import es.iaaa.kubby.services.DescribeEntityService
import es.iaaa.kubby.services.IndexService
import es.iaaa.kubby.services.impl.DescribeEntityServiceImpl
import es.iaaa.kubby.services.impl.IndexServiceImpl
import io.ktor.application.ApplicationEnvironment
import org.koin.dsl.module.Module
import org.koin.dsl.module.module

fun module(env: ApplicationEnvironment): Module {
    return module(createOnStart = false) {
        single { env.config.toProjectDescription() }
        single { env.config.toEntityRepository() }
        single<IndexService> { IndexServiceImpl(get(), get<ProjectDescription>().indexResource) }
        single<DescribeEntityService> { DescribeEntityServiceImpl(get(), get<ProjectDescription>().usePrefixes) }
        single { env.config.toRoutes() }
        single { env.toVelocityConfiguration() }
    }
}





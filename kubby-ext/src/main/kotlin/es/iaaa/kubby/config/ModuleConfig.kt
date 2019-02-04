package es.iaaa.kubby.config

import es.iaaa.kubby.repository.EntityRepository
import es.iaaa.kubby.services.DescribeEntityService
import es.iaaa.kubby.services.IndexService
import es.iaaa.kubby.services.impl.DescribeEntityServiceImpl
import es.iaaa.kubby.services.impl.IndexServiceImpl
import io.ktor.application.ApplicationEnvironment
import org.koin.dsl.module.Module
import org.koin.dsl.module.module

fun createKubbyModule(env: ApplicationEnvironment): Module {
    val conf = env.config.toProjectDescription()
    return module(createOnStart = false) {
        single<EntityRepository> {
            env.config.toEntityRepository()
        }
        single<IndexService> {
            IndexServiceImpl(get(), conf.indexResource)
        }
        single<DescribeEntityService> {
            DescribeEntityServiceImpl(
                entityRepository = get(),
                prefixes = conf.usePrefixes
            )
        }
        single {
            conf
        }
        single {
            env.config.toRoutes()
        }
        single {
            env.toVelocityConfiguration()
        }
    }
}





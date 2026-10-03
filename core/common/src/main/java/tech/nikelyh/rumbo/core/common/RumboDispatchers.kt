package tech.nikelyh.rumbo.core.common

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val rumboDispatcher: RumboDispatchers)

enum class RumboDispatchers {
    Default,
    IO,
    Main
}

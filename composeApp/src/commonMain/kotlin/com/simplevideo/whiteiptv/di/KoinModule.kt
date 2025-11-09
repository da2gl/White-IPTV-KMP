package com.simplevideo.whiteiptv.di

import com.simplevideo.whiteiptv.feature.onboarding.OnboardingViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin DI modules for WhiteIPTV app
 *
 * TODO: Add repository modules when data layer is implemented
 * TODO: Add use case modules when domain layer is implemented
 * TODO: Add network/database modules when needed
 */

/**
 * ViewModels module
 *
 * TODO: Add other feature ViewModels as they are implemented
 */
val viewModelModule = module {
    viewModelOf(::OnboardingViewModel)
}

/**
 * Repository module
 *
 * TODO: Implement when data layer is ready
 */
val repositoryModule = module {
    // Example: single<PlaylistRepository> { PlaylistRepositoryImpl(get()) }
}

/**
 * Use case module
 *
 * TODO: Implement when domain layer is ready
 */
val useCaseModule = module {
    // Example: factory { GetPlaylistsUseCase(get()) }
}

/**
 * Network module
 *
 * TODO: Implement when network layer is ready
 */
val networkModule = module {
    // Example: single { HttpClient() }
}

/**
 * Database module
 *
 * TODO: Implement when database layer is ready
 */
val databaseModule = module {
    // Example: single { DatabaseDriverFactory().createDriver() }
}

/**
 * All app modules combined
 */
val appModules: List<Module> = listOf(
    viewModelModule,
    repositoryModule,
    useCaseModule,
    networkModule,
    databaseModule,
)

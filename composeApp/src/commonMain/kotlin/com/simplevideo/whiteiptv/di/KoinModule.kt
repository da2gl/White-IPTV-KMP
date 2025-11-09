package com.simplevideo.whiteiptv.di

import com.simplevideo.whiteiptv.data.local.AppDatabase
import com.simplevideo.whiteiptv.data.local.getDatabaseBuilder
import com.simplevideo.whiteiptv.data.repository.PlaylistRepositoryImpl
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import com.simplevideo.whiteiptv.feature.onboarding.OnboardingViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::OnboardingViewModel)
}

val repositoryModule = module {
    singleOf(::PlaylistRepositoryImpl) bind PlaylistRepository::class
}

val useCaseModule = module {
    // Example: factory { GetPlaylistsUseCase(get()) }
}

val networkModule = module {
    single { HttpClient() }
}

val databaseModule = module {
    single { getDatabaseBuilder().build() }
    single { get<AppDatabase>().playlistDao() }
}

val appModules: List<Module> = listOf(
    viewModelModule,
    repositoryModule,
    useCaseModule,
    networkModule,
    databaseModule,
)

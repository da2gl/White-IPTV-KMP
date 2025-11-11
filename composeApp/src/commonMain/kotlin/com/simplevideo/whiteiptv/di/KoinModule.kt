package com.simplevideo.whiteiptv.di

import com.simplevideo.whiteiptv.data.local.AppDatabase
import com.simplevideo.whiteiptv.data.mapper.ChannelMapper
import com.simplevideo.whiteiptv.data.mapper.PlaylistMapper
import com.simplevideo.whiteiptv.data.network.HttpClientFactory
import com.simplevideo.whiteiptv.data.repository.PlaylistRepositoryImpl
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import com.simplevideo.whiteiptv.domain.usecase.ImportPlaylistUseCase
import com.simplevideo.whiteiptv.feature.onboarding.OnboardingViewModel
import com.simplevideo.whiteiptv.feature.splash.SplashViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::OnboardingViewModel)
}

val repositoryModule = module {
    singleOf(::PlaylistRepositoryImpl) bind PlaylistRepository::class
}

val mapperModule = module {
    factoryOf(::ChannelMapper)
    factoryOf(::PlaylistMapper)
}

val useCaseModule = module {
    factoryOf(::ImportPlaylistUseCase)
}

val networkModule = module {
    single { HttpClientFactory.create() }
}

val databaseModule = module {
    single { get<AppDatabase>().playlistDao() }
}

/**
 * Platform-specific module that provides platform dependencies
 * Android: provides AppDatabase with Context
 * iOS: provides AppDatabase with file path
 */
expect fun platformModule(): Module

val appModules: List<Module> = listOf(
    platformModule(),
    viewModelModule,
    repositoryModule,
    mapperModule,
    useCaseModule,
    networkModule,
    databaseModule,
)

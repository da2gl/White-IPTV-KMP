package com.simplevideo.whiteiptv.di

import com.simplevideo.whiteiptv.data.local.AppDatabase
import com.simplevideo.whiteiptv.data.mapper.ChannelMapper
import com.simplevideo.whiteiptv.data.mapper.PlaylistMapper
import com.simplevideo.whiteiptv.data.network.HttpClientFactory
import com.simplevideo.whiteiptv.data.repository.MockChannelsRepositoryImpl
import com.simplevideo.whiteiptv.data.repository.PlaylistRepositoryImpl
import com.simplevideo.whiteiptv.domain.repository.ChannelsRepository
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import com.simplevideo.whiteiptv.domain.usecase.GetChannelCategoriesUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetChannelsUseCase
import com.simplevideo.whiteiptv.domain.usecase.ImportPlaylistUseCase
import com.simplevideo.whiteiptv.domain.usecase.ToggleFavoriteStatusUseCase
import com.simplevideo.whiteiptv.feature.channels.ChannelsViewModel
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
    viewModelOf(::ChannelsViewModel)
}

val repositoryModule = module {
    singleOf(::PlaylistRepositoryImpl) bind PlaylistRepository::class
    singleOf(::MockChannelsRepositoryImpl) bind ChannelsRepository::class
}

val mapperModule = module {
    factoryOf(::ChannelMapper)
    factoryOf(::PlaylistMapper)
}

val useCaseModule = module {
    factoryOf(::ImportPlaylistUseCase)
    factoryOf(::GetChannelsUseCase)
    factoryOf(::GetChannelCategoriesUseCase)
    factoryOf(::ToggleFavoriteStatusUseCase)
}

val networkModule = module {
    single { HttpClientFactory.create() }
}

val databaseModule = module {
    single { get<AppDatabase>().playlistDao() }
}

/**
 * Platform-specific module that provides platform dependencies:
 * - AppDatabase (Android: with Context, iOS: with file path)
 * - FilePicker (Android: ActivityResultLauncher, iOS: UIDocumentPickerViewController)
 * - FileReader (Android: ContentResolver, iOS: FileManager)
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

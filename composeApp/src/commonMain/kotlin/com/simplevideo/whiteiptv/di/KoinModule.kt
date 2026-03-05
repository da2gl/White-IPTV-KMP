package com.simplevideo.whiteiptv.di

import com.russhwolf.settings.Settings
import com.simplevideo.whiteiptv.data.local.AppDatabase
import com.simplevideo.whiteiptv.data.local.ThemePreferences
import com.simplevideo.whiteiptv.data.mapper.ChannelGroupMapper
import com.simplevideo.whiteiptv.data.mapper.ChannelMapper
import com.simplevideo.whiteiptv.data.mapper.PlaylistMapper
import com.simplevideo.whiteiptv.data.network.HttpClientFactory
import com.simplevideo.whiteiptv.data.repository.ChannelRepositoryImpl
import com.simplevideo.whiteiptv.data.repository.PlaylistRepositoryImpl
import com.simplevideo.whiteiptv.data.repository.ThemeRepositoryImpl
import com.simplevideo.whiteiptv.data.repository.WatchHistoryRepositoryImpl
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import com.simplevideo.whiteiptv.domain.repository.CurrentPlaylistRepository
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import com.simplevideo.whiteiptv.domain.repository.ThemeRepository
import com.simplevideo.whiteiptv.domain.repository.WatchHistoryRepository
import com.simplevideo.whiteiptv.domain.usecase.DeletePlaylistUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetAdjacentChannelUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetChannelByIdUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetChannelsUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetContinueWatchingUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetFavoritesUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetGroupsUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetHomeCategoriesUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetPlaylistsUseCase
import com.simplevideo.whiteiptv.domain.usecase.HasPlaylistUseCase
import com.simplevideo.whiteiptv.domain.usecase.ImportPlaylistUseCase
import com.simplevideo.whiteiptv.domain.usecase.RecordWatchEventUseCase
import com.simplevideo.whiteiptv.domain.usecase.RenamePlaylistUseCase
import com.simplevideo.whiteiptv.domain.usecase.ToggleFavoriteUseCase
import com.simplevideo.whiteiptv.feature.channels.ChannelsViewModel
import com.simplevideo.whiteiptv.feature.favorites.FavoritesViewModel
import com.simplevideo.whiteiptv.feature.home.HomeViewModel
import com.simplevideo.whiteiptv.feature.onboarding.OnboardingViewModel
import com.simplevideo.whiteiptv.feature.player.PlayerViewModel
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
    viewModelOf(::HomeViewModel)
    viewModelOf(::FavoritesViewModel)
    viewModelOf(::ChannelsViewModel)
    viewModelOf(::PlayerViewModel)
}

val repositoryModule = module {
    singleOf(::PlaylistRepositoryImpl) bind PlaylistRepository::class
    singleOf(::ChannelRepositoryImpl) bind ChannelRepository::class
    singleOf(::CurrentPlaylistRepository)
    singleOf(::WatchHistoryRepositoryImpl) bind WatchHistoryRepository::class
}

val mapperModule = module {
    factoryOf(::ChannelMapper)
    factoryOf(::ChannelGroupMapper)
    factoryOf(::PlaylistMapper)
}

val useCaseModule = module {
    factoryOf(::ImportPlaylistUseCase)
    factoryOf(::HasPlaylistUseCase)
    factoryOf(::GetContinueWatchingUseCase)
    factoryOf(::GetFavoritesUseCase)
    factoryOf(::GetPlaylistsUseCase)
    factoryOf(::GetHomeCategoriesUseCase)
    factoryOf(::GetGroupsUseCase)
    factoryOf(::GetChannelsUseCase)
    factoryOf(::ToggleFavoriteUseCase)
    factoryOf(::GetChannelByIdUseCase)
    factoryOf(::GetAdjacentChannelUseCase)
    factoryOf(::RecordWatchEventUseCase)
    factoryOf(::RenamePlaylistUseCase)
    factoryOf(::DeletePlaylistUseCase)
}

val networkModule = module {
    single { HttpClientFactory.create() }
}

val databaseModule = module {
    single { get<AppDatabase>().playlistDao() }
    single { get<AppDatabase>().watchHistoryDao() }
}

val settingsModule = module {
    single { Settings() }
    singleOf(::ThemePreferences)
    singleOf(::ThemeRepositoryImpl) bind ThemeRepository::class
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
    settingsModule,
)

package com.simplevideo.whiteiptv.feature.onboarding

import com.simplevideo.whiteiptv.data.mapper.ChannelMapper
import com.simplevideo.whiteiptv.data.mapper.PlaylistMapper
import com.simplevideo.whiteiptv.data.repository.FakePlaylistRepository
import com.simplevideo.whiteiptv.data.repository.StubChannelRepository
import com.simplevideo.whiteiptv.domain.exception.PlaylistException
import com.simplevideo.whiteiptv.domain.usecase.ImportPlaylistUseCase
import com.simplevideo.whiteiptv.feature.onboarding.mvi.ImportError
import com.simplevideo.whiteiptv.feature.onboarding.mvi.OnboardingEvent
import com.simplevideo.whiteiptv.platform.FileReader
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private var useCaseException: Throwable? = null

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        useCaseException = null
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): OnboardingViewModel {
        val fakePlaylistRepository = FakePlaylistRepository()
        val stubChannelRepository = StubChannelRepository()
        val useCase = ImportPlaylistUseCase(
            playlistRepository = fakePlaylistRepository,
            channelRepository = stubChannelRepository,
            httpClient = HttpClient(),
            fileReader = object : FileReader {
                override suspend fun readFile(uri: String): String {
                    useCaseException?.let { throw it }
                    return ""
                }
            },
            channelMapper = ChannelMapper(),
            playlistMapper = PlaylistMapper(),
        )
        return OnboardingViewModel(useCase)
    }

    private fun triggerFileImport(viewModel: OnboardingViewModel) {
        viewModel.obtainEvent(OnboardingEvent.FileSelected("test.m3u", "file:///test.m3u"))
    }

    @Test
    fun `network timeout maps to ImportError Timeout`() = runTest {
        useCaseException = PlaylistException.NetworkError("Request timeout")
        val viewModel = createViewModel()

        triggerFileImport(viewModel)
        advanceUntilIdle()

        assertIs<ImportError.Timeout>(viewModel.viewStates().value.error)
    }

    @Test
    fun `connection error maps to ImportError NoConnection`() = runTest {
        useCaseException = PlaylistException.NetworkError("Connection failed. Check your internet connection")
        val viewModel = createViewModel()

        triggerFileImport(viewModel)
        advanceUntilIdle()

        assertIs<ImportError.NoConnection>(viewModel.viewStates().value.error)
    }

    @Test
    fun `HTTP 404 maps to ImportError HttpError with code 404`() = runTest {
        useCaseException = PlaylistException.NetworkError("Failed to download playlist: HTTP 404")
        val viewModel = createViewModel()

        triggerFileImport(viewModel)
        advanceUntilIdle()

        val error = viewModel.viewStates().value.error
        assertIs<ImportError.HttpError>(error)
        assertEquals(404, error.statusCode)
    }

    @Test
    fun `invalid URL maps to ImportError InvalidUrl`() = runTest {
        useCaseException = PlaylistException.InvalidUrl("bad-url")
        val viewModel = createViewModel()

        triggerFileImport(viewModel)
        advanceUntilIdle()

        assertIs<ImportError.InvalidUrl>(viewModel.viewStates().value.error)
    }

    @Test
    fun `parse error maps to ImportError InvalidFormat`() = runTest {
        useCaseException = PlaylistException.ParseError()
        val viewModel = createViewModel()

        triggerFileImport(viewModel)
        advanceUntilIdle()

        assertIs<ImportError.InvalidFormat>(viewModel.viewStates().value.error)
    }

    @Test
    fun `empty playlist maps to ImportError EmptyPlaylist`() = runTest {
        useCaseException = PlaylistException.EmptyPlaylist()
        val viewModel = createViewModel()

        triggerFileImport(viewModel)
        advanceUntilIdle()

        assertIs<ImportError.EmptyPlaylist>(viewModel.viewStates().value.error)
    }

    @Test
    fun `database error maps to ImportError StorageError`() = runTest {
        useCaseException = PlaylistException.DatabaseError()
        val viewModel = createViewModel()

        triggerFileImport(viewModel)
        advanceUntilIdle()

        assertIs<ImportError.StorageError>(viewModel.viewStates().value.error)
    }

    @Test
    fun `unknown exception maps to ImportError Unknown`() = runTest {
        useCaseException = RuntimeException("something broke")
        val viewModel = createViewModel()

        triggerFileImport(viewModel)
        advanceUntilIdle()

        assertIs<ImportError.Unknown>(viewModel.viewStates().value.error)
    }

    @Test
    fun `network error with default message maps to NoConnection`() = runTest {
        useCaseException = PlaylistException.NetworkError()
        val viewModel = createViewModel()

        triggerFileImport(viewModel)
        advanceUntilIdle()

        assertIs<ImportError.NoConnection>(viewModel.viewStates().value.error)
    }

    @Test
    fun `NotFound maps to ImportError StorageError`() = runTest {
        useCaseException = PlaylistException.NotFound(99L)
        val viewModel = createViewModel()

        triggerFileImport(viewModel)
        advanceUntilIdle()

        assertIs<ImportError.StorageError>(viewModel.viewStates().value.error)
    }

    @Test
    fun `HTTP error with no valid code maps to NoConnection`() = runTest {
        useCaseException = PlaylistException.NetworkError("HTTP error without code")
        val viewModel = createViewModel()

        triggerFileImport(viewModel)
        advanceUntilIdle()

        assertIs<ImportError.NoConnection>(viewModel.viewStates().value.error)
    }

    @Test
    fun `HTTP 500 maps to ImportError HttpError with code 500`() = runTest {
        useCaseException = PlaylistException.NetworkError("Failed to download playlist: HTTP 500")
        val viewModel = createViewModel()

        triggerFileImport(viewModel)
        advanceUntilIdle()

        val error = viewModel.viewStates().value.error
        assertIs<ImportError.HttpError>(error)
        assertEquals(500, error.statusCode)
    }

    @Test
    fun `Unknown PlaylistException maps to ImportError Unknown with detail`() = runTest {
        useCaseException = PlaylistException.Unknown("something unexpected")
        val viewModel = createViewModel()

        triggerFileImport(viewModel)
        advanceUntilIdle()

        val error = viewModel.viewStates().value.error
        assertIs<ImportError.Unknown>(error)
        assertEquals("something unexpected", error.detail)
    }

    @Test
    fun `entering new URL clears error`() = runTest {
        useCaseException = PlaylistException.ParseError()
        val viewModel = createViewModel()

        triggerFileImport(viewModel)
        advanceUntilIdle()

        assertIs<ImportError.InvalidFormat>(viewModel.viewStates().value.error)

        viewModel.obtainEvent(OnboardingEvent.EnterPlaylistUrl("https://new-url.com"))

        assertNull(viewModel.viewStates().value.error)
    }
}

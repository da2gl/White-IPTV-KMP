package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupCrossRef
import com.simplevideo.whiteiptv.data.repository.FakeChannelRepository
import com.simplevideo.whiteiptv.domain.model.ChannelsFilter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetChannelsUseCaseTest {

    private lateinit var fakeRepository: FakeChannelRepository
    private lateinit var useCase: GetChannelsUseCase

    private val testChannels = listOf(
        createChannel(1L, "CNN News", playlistId = 1L),
        createChannel(2L, "BBC World", playlistId = 1L),
        createChannel(3L, "ESPN Sports", playlistId = 2L),
        createChannel(4L, "Discovery Channel", playlistId = 2L),
        createChannel(5L, "CNN International", playlistId = 1L),
    )

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeChannelRepository()
        useCase = GetChannelsUseCase(fakeRepository)
        fakeRepository.setChannels(testChannels)
        fakeRepository.addCrossRefs(
            listOf(
                ChannelGroupCrossRef(channelId = 1L, groupId = 10L),
                ChannelGroupCrossRef(channelId = 2L, groupId = 10L),
                ChannelGroupCrossRef(channelId = 3L, groupId = 20L),
            ),
        )
    }

    // --- Empty query routing (should call non-search methods) ---

    @Test
    fun `empty query with All filter calls getAllChannels`() = runTest {
        useCase(filter = ChannelsFilter.All, query = "").first()

        assertTrue(fakeRepository.methodCalls.contains("getAllChannels"))
        assertTrue(fakeRepository.methodCalls.none { it.startsWith("search") })
    }

    @Test
    fun `empty query with ByPlaylist filter calls getChannelsByPlaylistId`() = runTest {
        useCase(filter = ChannelsFilter.ByPlaylist(1L), query = "").first()

        assertTrue(fakeRepository.methodCalls.contains("getChannelsByPlaylistId(1)"))
        assertTrue(fakeRepository.methodCalls.none { it.startsWith("search") })
    }

    @Test
    fun `empty query with ByGroup filter calls getChannelsByGroupId`() = runTest {
        useCase(filter = ChannelsFilter.ByGroup(10L), query = "").first()

        assertTrue(fakeRepository.methodCalls.contains("getChannelsByGroupId(10)"))
        assertTrue(fakeRepository.methodCalls.none { it.startsWith("search") })
    }

    @Test
    fun `default parameters call getAllChannels`() = runTest {
        useCase().first()

        assertTrue(fakeRepository.methodCalls.contains("getAllChannels"))
    }

    // --- Non-empty query routing (should call search methods) ---

    @Test
    fun `non-empty query with All filter calls searchChannels`() = runTest {
        useCase(filter = ChannelsFilter.All, query = "CNN").first()

        assertTrue(fakeRepository.methodCalls.contains("searchChannels(CNN)"))
        assertTrue(fakeRepository.methodCalls.none { it == "getAllChannels" })
    }

    @Test
    fun `non-empty query with ByPlaylist filter calls searchChannelsByPlaylistId`() = runTest {
        useCase(filter = ChannelsFilter.ByPlaylist(1L), query = "CNN").first()

        assertTrue(fakeRepository.methodCalls.contains("searchChannelsByPlaylistId(CNN, 1)"))
    }

    @Test
    fun `non-empty query with ByGroup filter calls searchChannelsByGroupId`() = runTest {
        useCase(filter = ChannelsFilter.ByGroup(10L), query = "BBC").first()

        assertTrue(fakeRepository.methodCalls.contains("searchChannelsByGroupId(BBC, 10)"))
    }

    // --- Query trimming ---

    @Test
    fun `whitespace-only query is treated as empty`() = runTest {
        useCase(filter = ChannelsFilter.All, query = "   ").first()

        assertTrue(fakeRepository.methodCalls.contains("getAllChannels"))
        assertTrue(fakeRepository.methodCalls.none { it.startsWith("search") })
    }

    @Test
    fun `query with surrounding whitespace is trimmed`() = runTest {
        useCase(filter = ChannelsFilter.All, query = "  CNN  ").first()

        assertTrue(fakeRepository.methodCalls.contains("searchChannels(CNN)"))
    }

    // --- Result correctness ---

    @Test
    fun `search returns matching channels for All filter`() = runTest {
        val result = useCase(filter = ChannelsFilter.All, query = "CNN").first()

        assertEquals(2, result.size)
        assertTrue(result.all { it.name.contains("CNN", ignoreCase = true) })
    }

    @Test
    fun `search returns matching channels filtered by playlist`() = runTest {
        val result = useCase(filter = ChannelsFilter.ByPlaylist(1L), query = "CNN").first()

        assertEquals(2, result.size)
        assertTrue(result.all { it.playlistId == 1L })
        assertTrue(result.all { it.name.contains("CNN", ignoreCase = true) })
    }

    @Test
    fun `search returns matching channels filtered by group`() = runTest {
        val result = useCase(filter = ChannelsFilter.ByGroup(10L), query = "BBC").first()

        assertEquals(1, result.size)
        assertEquals("BBC World", result[0].name)
    }

    @Test
    fun `search with no matches returns empty list`() = runTest {
        val result = useCase(filter = ChannelsFilter.All, query = "NonExistent").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `empty query returns all channels in scope`() = runTest {
        val result = useCase(filter = ChannelsFilter.All, query = "").first()

        assertEquals(5, result.size)
    }

    @Test
    fun `search is case insensitive`() = runTest {
        val result = useCase(filter = ChannelsFilter.All, query = "cnn").first()

        assertEquals(2, result.size)
        assertTrue(result.all { it.name.contains("CNN", ignoreCase = true) })
    }

    private fun createChannel(
        id: Long,
        name: String,
        playlistId: Long = 1L,
    ): ChannelEntity = ChannelEntity(
        id = id,
        playlistId = playlistId,
        name = name,
        url = "http://example.com/stream/$id",
    )
}

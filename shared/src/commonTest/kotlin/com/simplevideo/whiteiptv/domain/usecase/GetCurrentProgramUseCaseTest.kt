package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.repository.FakeEpgRepository
import com.simplevideo.whiteiptv.domain.model.EpgProgram
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GetCurrentProgramUseCaseTest {

    private lateinit var fakeRepository: FakeEpgRepository
    private lateinit var useCase: GetCurrentProgramUseCase

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeEpgRepository()
        useCase = GetCurrentProgramUseCase(fakeRepository)
    }

    @Test
    fun `returns null pair when tvgId is null`() = runTest {
        val (current, next) = useCase(tvgId = null)

        assertNull(current)
        assertNull(next)
    }

    @Test
    fun `returns null pair when tvgId is blank`() = runTest {
        val (current, next) = useCase(tvgId = "   ")

        assertNull(current)
        assertNull(next)
    }

    @Test
    fun `returns null pair when tvgId is empty string`() = runTest {
        val (current, next) = useCase(tvgId = "")

        assertNull(current)
        assertNull(next)
    }

    @Test
    fun `delegates to repository for valid tvgId`() = runTest {
        val currentProgram = EpgProgram(
            title = "Current Show",
            startTimeMs = 1000L,
            endTimeMs = 2000L,
        )
        val nextProgram = EpgProgram(
            title = "Next Show",
            startTimeMs = 2000L,
            endTimeMs = 3000L,
        )
        fakeRepository.currentProgram = currentProgram
        fakeRepository.nextProgram = nextProgram

        val (current, next) = useCase(tvgId = "channel.one")

        assertNotNull(current)
        assertEquals("Current Show", current.title)
        assertNotNull(next)
        assertEquals("Next Show", next.title)
    }

    @Test
    fun `returns current without next when only current exists`() = runTest {
        fakeRepository.currentProgram = EpgProgram(
            title = "Only Current",
            startTimeMs = 1000L,
            endTimeMs = 2000L,
        )
        fakeRepository.nextProgram = null

        val (current, next) = useCase(tvgId = "channel.one")

        assertNotNull(current)
        assertEquals("Only Current", current.title)
        assertNull(next)
    }

    @Test
    fun `returns null pair when repository has no data`() = runTest {
        fakeRepository.currentProgram = null
        fakeRepository.nextProgram = null

        val (current, next) = useCase(tvgId = "channel.one")

        assertNull(current)
        assertNull(next)
    }
}

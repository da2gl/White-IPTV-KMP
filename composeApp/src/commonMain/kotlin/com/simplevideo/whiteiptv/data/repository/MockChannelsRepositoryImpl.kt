package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.domain.model.Channel
import com.simplevideo.whiteiptv.domain.model.ChannelCategory
import com.simplevideo.whiteiptv.domain.repository.ChannelsRepository

class MockChannelsRepositoryImpl : ChannelsRepository {

    private val channels = mutableListOf(
        Channel("1", "Channel One", "https://lh3.googleusercontent.com/aida-public/AB6AXuABhuGZJowfBi4QHAKsH1u1BmAy149ufr7ZB8d_D6ddXw59MyuiEcReBp8TzdaZfky_lllsgHJDS_9v1nP1NbrBKYehQqJfRk1IcN6o_4OQ0Mmh-Wk2_56J3dYJpYls7jkZkn7BB5jq9BQIkScJ1Gv3nHmmsptnHy5wLo3xNZCgZE1tkQ4sNM9KvLT6lZcWIdpm2D2Nr0XK8m6BjRydabsJYzmP2Xl-vkLwjSuCg2aOubgvVND__ZbvgqcWQjLqOv0cI1jtkHWarYxZ", "Movies", false),
        Channel("2", "Channel Two", "https://lh3.googleusercontent.com/aida-public/AB6AXuD5ASNmaUe7WphyaI7qjQxGZIN17pL0byvEIU14QJ_htkucJllEtCE1Gefj7tpyYIXmcfe4vzB0DhqrEO62uaoY0Tj-tfSM9NfLHmjxCifGDFmujv_YwLacy409QdpUWEAUNq-Ddhjz5BzRlC8jjceI5mUGcRjHwQvKtukAKgStBPXDB-rcplSZk_ucVJyd3c6shAkz_7lknxuhoDswflXiq4G0TG1rbGJN-cqiUEg6p2eDmvpaXa0HOUtKVPbIxRAu1CRzhbCp7Jml", "Sports", true),
        Channel("3", "Channel Three", "https://lh3.googleusercontent.com/aida-public/AB6AXuB2ZborWJ-EiRbpAxkeoml7HBpMtjtQIo4-dMPp5aTrZkg5zwv3rakEy-8P0dOSWA47DDwy-pzm1azGkwiNrejwnEF4P4EWcgmWQoVgW1zrP4Dh3YFG2pMtLHVwJ1EVtgCl_I5ccie18Ljfn-TqAAdz11sWBe3lmpgZ4QanTcR8K0sx76JJvbWTSh-cVo5nm_h35BMf6L1L1MZrnyeXO1oI5FWXcx0d56EYat_qGfpz0aja2HHM2iVr8HJidbh-EJJh3XRHVuS6nsCQ", "News", false),
        Channel("4", "Channel Four", "https://lh3.googleusercontent.com/aida-public/AB6AXuDITZFcsgxgIFuWMZs4N8QqAp8Vo5eIOju56_6-k2u6zLJVY_JDGnIjtSgKU1MBBqkaM-r-u5YC9X4Q48gZdCt5IZrWLtWkHTBR5sES4nI2uxOWAATBZZ5jYK0HrnVRrPqJgvmI1MApTdyduQNcDgrJGTpDlSNkc4I-TeNrRD7JV9Odk_eeTNOLuSyQFjABaYQyJglVLthHQRHqaDMkro7njq4j_UxNK--R5qjuZJ03Vj3sHpjZE4E7ovme8reN_8fEAlTg0vVVgs7q", "Kids", false),
        Channel("5", "Channel Five", "https://lh3.googleusercontent.com/aida-public/AB6AXuA4MTt3HzQms8NlPgJRS1M2UCtnE7-i4g116bMLM0ItvoT47uAyeMfF-kmr4ApjB7W5vAUYCnjkp-dwM84fa021pJYd3jYYivNiGnjekqSuz7LJXClynI6cMYx6WR7Jmomgi-4n-wMHxZ-z1X-nzWLxTASAXHU-Ns7ML-E0kq0E-ASy9i6TXWGUV5rumooQFdb50bXMqsCz5pV1tfwc22RviLEu4iQgaJQ9SIaYYV-cXMpm8U6BeLIAyM6jyAejEa-ohoEgWiLZ6G6N", "Sports", true),
        Channel("6", "Channel Six", "https://lh3.googleusercontent.com/aida-public/AB6AXuArElts-5yb4-oCSAOPiglCGpVdyv3EgHOSv3vXenCW4AiZX-FB6t0LhUFW0CWrP9zux4bGMhxuanvb4QU5r4hdFO4cAtEZbvBsN36mOUzSoA_wESiUT95BdcjKdHhPHg1TX-tQjMmiNNSeCzJmVSsgokGNKiu8emhNMp7K0IyfDm0NHvAsLOm3czUIlgUudexeSafUs3vuy4BZCqFQyiaEEXSChBMUkkB_J75Z_MBChy_tU1r34gO6ZjwqmDTxiWzU8ljMFf-GtREQ", "Movies", false),
    )

    override suspend fun getChannels(): List<Channel> {
        return channels
    }

    override suspend fun getChannelCategories(): List<ChannelCategory> {
        return channels.map { ChannelCategory(it.category) }.distinct()
    }

    override suspend fun toggleFavoriteStatus(channelId: String): Channel {
        val channel = channels.find { it.id == channelId }
        val index = channels.indexOf(channel)
        if (channel != null) {
            val newChannel = channel.copy(isFavorite = !channel.isFavorite)
            channels[index] = newChannel
            return newChannel
        }
        throw NoSuchElementException("Channel with id $channelId not found")
    }
}

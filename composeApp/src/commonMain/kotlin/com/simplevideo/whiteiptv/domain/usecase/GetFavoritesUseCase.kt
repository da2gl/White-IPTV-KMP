package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.model.Channel

class GetFavoritesUseCase {
    operator fun invoke(): List<Channel> = listOf(
        Channel(
            name = "HBO",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAKNgBUeiM8z-1syloqKw3ke46eeu-boF2G8wVgnJV_5QwuptGQwTV3leqw9gzaQFeUiCyRodInlbkpSYjrmokffU1I7faA77Q9KMYMDpClvpreKrEaPT1l55pUxdXfTWP_Ocni9w7hOrl9DsqULR6uvA8sG285uLf05VoO5B_Swop6V20AbIOSUAjqwSuMEZDwwPYmQlJ0C_W7CvdR-L0MRfERKGP3frHKG43-DcvXdyCEhZ1tB1_hVOzCNPTWr0C1CmItT0fyhGJ-",
            isLive = true
        ),
        Channel(
            name = "ESPN",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuChMzrNsW2n-P986Bp1w4uMZKI0KmuBbJkEUQP12a3oVfGhlJGKK9i79x1ILcUVqSeleFGb0NPDZqSNq8MGJnHNWIzzswH3l8IOf_G3uTZnsXR2Oe8heHqCejDFNZomC6l9kMyz8PbVQIv_pcmSTTvNKSvSGIA93hRDEaH0z21R3GHs3bXZhq50NKElKUqI6p0l-JhM-9a6WwHyLkP_a7oTi4FJahl6oko-4v9qr7EBVgkGyscNlGmzb3xRO1zIW3xe5sgtjbz9GJGZ",
            isLive = true
        ),
        Channel(
            name = "BBC World News",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBt2ItX-4Tot2sLXGGNiW6PIPz9Z3HGkXRTREGse_r1yOXfc1hU8S1LJRAqH-mb_379NWFj6QvgofZeuPD7kyEHOtYNFwaumir9ElT6l1O6IPj9gkFf91FV_7u-SWpQsdj0TuKSpuZ_Kd_vnHLqYHrFR3y0QtNyqMc3x61ZM9KoNErDDRIhB2xNIICEka7d-mqwHERthhpOG5xwNMhHaOkEWevcQjig2ghRDMeOizR63QsNMu3pZvJhEk4oERYJHGHo9OWYIcTVE9i5",
            isLive = true
        ),
        Channel(
            name = "Cinema Max",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuADxZL25ePdVr8VF79r8Z3n95MFajSQumNPMaycOzJWPwTsVS20ja8k01YmoT64vYLjHkCqShSOqQ0pmDda3ANiYRNdVUVrUUtV2uAMPaKmdnl6VFJZclfU6jF8w6C8EDROin-q6LBGyGLhguuD8bRDP8icVlRmCy9Kd1pxht7E5_Umpm-Y5MvEDDc8jYKuLyckl1zcP3eNXTcMCvYmwereAZTzPFZ4RPWwq7D42OSRCHagph9SDagYFPcPMgwxae2M8CHgNVhA1Vy0",
            isLive = true
        )
    )
}

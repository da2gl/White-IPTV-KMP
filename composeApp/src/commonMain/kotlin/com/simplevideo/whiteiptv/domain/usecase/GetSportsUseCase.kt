package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.model.Channel

class GetSportsUseCase {
    operator fun invoke(): List<Channel> = listOf(
        Channel(
            name = "Fox Sports 1",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB-GxsSd1senwi2QyaAsD05D0NHZxzUZ30Z_8QnPcYuYsNxC5U3EdTeRzJkzN0EaQgA4L-BfXB0qZ3X_8JDZyECWqRlqhrktE9ulRHwzjbR2vZlePv1iKKJewR2oKUj3VDtw9jofVemYyshAUK4CkBvvXOShJTzad_AVwVRLA652lY_wM1LC9Gra2qUG-7Ff9-ygJbvNzzIPoC0TfPbrlmgMefqqRhACEaZujMciJ90vW4-KghwOAR_Vmjo0xUkKgFVSnzH8dnzK4sE",
            isLive = true
        ),
        Channel(
            name = "Tennis Channel",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDClKps8pR4rukM_921nWLkjhrK0lE6rTGlho4Z1vL4W0QclleXz8lfKIOEQ0czlcODBMr0vItAv4SYIWfsDLAkxjhWqOaTSoYLdaBDgLkiv4bJ6wa-PvFzBSrCrAuwSX967N1H-yjDerV2lwh5n1c3-mzo9t__xbHoYQGDi5HJWEH8yG9Z_PJmG0EOmNy9m_55KyS4B0mx79Czq_yyD_iW2YinVhMmYEzgXQ4d2QBRGr8vy1vm5mQJQoZrEC2ku0u_U274J3XJkwM9",
            isLive = true
        ),
        Channel(
            name = "Fight TV",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD7UVPtTwZKwo55I0WxvPFByyFvbLmkMBYsT0BjBSNv5xhDAavoFwzLqxoy1dMCOtJbkDvTVT3yLqnZ0hPEW-Y8mwzkfanso5LNWwL8O8OZuuwc53jzPu0O9DHUyYpd1V-bBzKoFsAxQ-hYPmFaH8ZdBQ3WAo2iGKI5p5HYACcRkfcAou6Mv4go3MLrt7Wgtk2XEEqaOhIjk1rxQLM7hAOnTCG62EvdmPhexbtoJDkvf0a6hXXf6Na5dvrT8MdsfChDLok3kq1AplkE",
            isLive = true
        ),
        Channel(
            name = "Golf Channel",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCkoGXxX_yGvkipcSOyFrHtTu-eQUnXPxb0p2Vt07z0zHJ9uEN5iUo_ZOfv1Qe6qFBntCSgh3LCpU6Cgn7QWYnA2cyUUTVaWvzxVxmGrieeW7MRpdyP-oqWoQifC4JKQQYdT8DChbAjMBwJvH71YIQCTX8a14Izj9nGfela0WLUDxBLoAL0mKagb0FnMdQwlpXaXYeYiBxr6eKluBlWMdwK2qx_QZIEZws1Ce7Tlq6UTxXxKEro-IO_hFQFe1OXDEM1AyAM6tBBJaaB",
            isLive = true
        )
    )
}

package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.model.ContinueWatchingItem

class GetContinueWatchingUseCase {
    operator fun invoke(): List<ContinueWatchingItem> = listOf(
        ContinueWatchingItem(
            name = "BBC News",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC2LS8R3xa35fLvxoc4Gi2RVsNXWKnojWAGzP5zc2zQAu45xDEArKrmXGizjj90OFZcqbXK9cmC1CG44IBAdYT6gFsVEs8Gci03JZiO52FXmvC8dGQSThxCypNewJBeZW_ab-ejLdZ5TefrOZHPPXoV07rlMcvp08n0Dtb8ceOl-gTevoDUA8wvcf9PpgSch4yckYMm43S1YcCEv8YPkTCdjCJvPx2fIMkCYDn0gMgeQCuCI3PYCbmW2dqo6WZfNow3zLPQbPKAslTv",
            progress = 0.66f,
            timeLeft = "24m left"
        ),
        ContinueWatchingItem(
            name = "ESPN",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAjTnyUyvFyB04w0CNInHag1o7AtA32eyXMMky8BQpKubNKLh-cdIEIvwlMkT5H9eUrU8ldVAnsVUQBx8IkR2ZRsb0M4wOp-jwUXxqYjYOcdPmp6sEYIcSGuXPvZgnoaR42uJZ-ZAI4exG9kgAFFz-bW_Byo3IYnWnujqRlpSM5QGFh-VrGrSAQ45ZyPIfudJ0I6oU72e9tzW9gPV6AUK0eJ8ckpN8QvsJS6Wh5CtVVi_lhDZCqH-eMSikmr9Abqmpbs8Bx4jvCBU3X",
            progress = 0.25f,
            timeLeft = "1h 15m left"
        )
    )
}

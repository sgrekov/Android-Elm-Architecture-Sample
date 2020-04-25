package com.sample.android.mobius.domain.main

import com.spotify.mobius.Effects
import com.spotify.mobius.Next
import com.spotify.mobius.Update


class MainUpdate : Update<MainModel, MainEvent, MainEffect> {
    override fun update(model: MainModel, event: MainEvent): Next<MainModel, MainEffect> {
        return when (event) {
            is MainInit -> Next.next(
                model, Effects.effects(LoadReposEffect)
            )
            is ReposLoadedEvent -> Next.next(model.copy(isLoading = false, reposList = event.reposList))
            else -> Next.noChange()
        }
    }
}


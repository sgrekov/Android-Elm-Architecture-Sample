package com.sample.android.mobius.domain.main

import org.eclipse.egit.github.core.Repository

data class MainModel(val isLoading: Boolean = true,
                     val userName: String,
                     val reposList: List<Repository> = listOf())

sealed class MainEffect
object LoadReposEffect : MainEffect()

sealed class MainEvent
object MainInit : MainEvent()
data class ReposLoadedEvent(val reposList: List<Repository>) : MainEvent()
object IdleEvent : MainEvent()
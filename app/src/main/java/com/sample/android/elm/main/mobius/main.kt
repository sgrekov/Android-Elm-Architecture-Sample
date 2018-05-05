package com.sample.android.elm.main.mobius

import org.eclipse.egit.github.core.Repository

data class MainModel(val isLoading: Boolean = true,
                     val userName: String,
                     val reposList: List<Repository> = listOf())

sealed class MainEffect
data class LoadReposEffect(val userName: String) : MainEffect()

sealed class MainEvent
object MainInit : MainEvent()
data class ReposLoadedEvent(val reposList: List<Repository>) : MainEvent()
object IdleEvent : MainEvent()
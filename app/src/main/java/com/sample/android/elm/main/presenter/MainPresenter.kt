package com.sample.android.elm.main.presenter

import com.sample.android.elm.*
import com.sample.android.elm.data.AppPrefs
import com.sample.android.elm.data.GitHubService
import com.sample.android.elm.main.view.IMainView
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import org.eclipse.egit.github.core.Repository

class MainPresenter(val view: IMainView,
                    val program: Program,
                    val appPrefs: AppPrefs,
                    val service: GitHubService) : Component {

    data class MainState(val isLoading: Boolean = true,
                         val userName: String,
                         val reposList: List<Repository> = listOf()) : State()

    data class LoadReposCmd(val userName: String) : Cmd()

    data class ReposLoadedMsg(val reposList: List<Repository>) : Msg()

    lateinit var disposable: Disposable

    fun init(initialState: MainState?) {
        disposable =
                program.init(initialState ?: MainState(userName = service.userName), this)

        initialState ?: program.accept(Init) //if no saved state, then run init Msg
    }

    override fun update(msg: Msg, state: State): Pair<State, Cmd> {
        val state = state as MainState
        return when (msg) {
            is Init -> Pair(state, LoadReposCmd(state.userName))
            is ReposLoadedMsg -> Pair(state.copy(isLoading = false, reposList = msg.reposList), None)
            else -> Pair(state, None)
        }
    }

    override fun render(state: State) {
        (state as MainState).apply {
            view.setTitle(state.userName + "'s starred repos")

            if (isLoading) {
                if (reposList.isEmpty()) {
                    view.showProgress()
                }
            } else {
                view.hideProgress()
                if (reposList.isEmpty()) {
                    view.setErrorText("User has no starred repos")
                    view.showErrorText()
                }
            }
            view.setRepos(reposList)
        }
    }

    override fun call(cmd: Cmd): Single<Msg> {
        return when (cmd) {
            is LoadReposCmd -> service.getStarredRepos(cmd.userName).map { repos -> ReposLoadedMsg(repos) }
            else -> Single.just(Idle)
        }
    }

    fun destroy() {
        disposable.dispose()
    }

    fun getState() : MainState{
        return program.getState() as MainState
    }

    fun render(){
        program.render()
    }

}

package com.sample.android.elm

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.*


sealed class AbstractState
open class State : AbstractState()

sealed class AbstractMsg
open class Msg : AbstractMsg()
class Idle : Msg()
class Init : Msg()
class ErrorMsg(val err : Throwable, val cmd : Cmd) : Msg()


sealed class AbstractCmd
open class Cmd : AbstractCmd()
class None : Cmd()

interface Component {

    fun update(msg: Msg, state: State): Pair<State, Cmd>

    fun render(state: State)

    fun call(cmd: Cmd): Single<Msg>

}


class ElmProgram(val outputScheduler: Scheduler) {

    private val msgRelay: BehaviorRelay<Pair<Msg, State>> = BehaviorRelay.create()
    private var msgQueue = ArrayDeque<Msg>()
    lateinit private var state: State
    private lateinit var component: Component

    fun init(initialState: State, component: Component): Disposable {
        this.component = component
        this.state = initialState
        return msgRelay
                .map { (msg, state) ->
                    Timber.d("elm reduce state:$state msg:$msg ")
                    component.update(msg, state)
                }
                .observeOn(outputScheduler)
                .doOnNext { (state, cmd) ->
                    component.render(state)
                }
                .flatMap { (state, cmd) ->
                    when (cmd) {
                        is None -> Observable.just((Pair(Idle(), state)))
                        else -> component.call(cmd).map { msg -> Pair(msg, state) }
                                .onErrorResumeNext{err -> Single.just(Pair(ErrorMsg(err, cmd), state)) }
                                .toObservable()
                    }
                }
                .observeOn(outputScheduler)
                .subscribe({ (msg, state) ->
                    this.state = state
                    Timber.d("elm event:${msg.javaClass.simpleName}")
                    Timber.d("elm msg queue:${msgQueue}")
                    when (msg) {
                        is Idle -> {
                            Timber.d("elm no new msg")
                        }
                        else -> accept(msg)
                    }
                    if (msgQueue.size > 0) {
                        msgQueue.removeFirst()
                    }

                    loop(state)
                })
    }

    fun getState(): State {
        return state
    }

    private fun loop(state: State) {
        Timber.d("elm loop")
        if (msgQueue.size > 0) {
            Timber.d("elm loop event:$msgQueue.first state:$state")
            msgRelay.accept(Pair(msgQueue.first, state))
        }
    }

    fun accept(msg: Msg) {
        Timber.d("elm accept event:${msg.javaClass.simpleName}")
        msgQueue.addLast(msg)
        if (msgQueue.size == 1) {
            Timber.d("elm relay accept:${msgQueue.first}")
            msgRelay.accept(Pair(msgQueue.first, state))
        }
    }

}
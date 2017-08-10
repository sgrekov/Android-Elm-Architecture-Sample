package com.sample.android.elm

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*


sealed class AbstractState
open class State : AbstractState()

sealed class AbstractMsg
open class Msg : AbstractMsg()
class Idle : Msg()
class Init : Msg()
class ErrorMsg(val err: Throwable, val cmd: Cmd) : Msg()


sealed class AbstractCmd
open class Cmd : AbstractCmd()
class None : Cmd()

interface Component {

    fun update(msg: Msg, state: State): Pair<State, Cmd>

    fun render(state: State)

    fun call(cmd: Cmd): Single<Msg>

}


class Program(val outputScheduler: Scheduler) {

    private val msgRelay: BehaviorRelay<Pair<Msg, State>> = BehaviorRelay.create()
    private var msgQueue = ArrayDeque<Msg>()
    lateinit private var state: State
    private lateinit var component: Component

    fun init(initialState: State, component: Component): Disposable {
        this.component = component
        this.state = initialState
        return msgRelay
                .map { (msg, state) ->
                    Timber.d("elm update msg:$msg ")
                    component.update(msg, state)
                }
                .observeOn(outputScheduler)
                .doOnNext { (state, cmd) ->
                    component.render(state)
                }
                .doOnNext{ (state, _) ->
                    this.state = state
                    if (msgQueue.size > 0) {
                        msgQueue.removeFirst()
                    }
                    loop()
                }
                .observeOn(Schedulers.io())
                .flatMap { (state, cmd) ->
                    Timber.d("call cmd:$cmd state:$state ")
                    when (cmd) {
                        is None -> Observable.just((Idle()))
                        else -> component.call(cmd)
                                .onErrorResumeNext { err -> Single.just(ErrorMsg(err, cmd)) }
                                .toObservable()
                    }
                }
                .observeOn(outputScheduler)
                .subscribe({ msg ->
                    Timber.d("elm subscribe msg:${msg.javaClass.simpleName}")
                    when (msg) {
                        is Idle -> {
                        }
                        else -> msgQueue.addLast(msg)
                    }

                    loop()
                })
    }

    fun getState(): State {
        return state
    }

    private fun loop() {
        if (msgQueue.size > 0) {
            msgRelay.accept(Pair(msgQueue.first, this.state))
        }
    }

    fun accept(msg: Msg) {
        Timber.d("elm accept event:${msg.javaClass.simpleName}")
        msgQueue.addLast(msg)
        if (msgQueue.size == 1) {
            msgRelay.accept(Pair(msgQueue.first, state))
        }
    }

}
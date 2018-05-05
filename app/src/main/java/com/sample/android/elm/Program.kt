package com.sample.android.elm

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.ArrayDeque


sealed class AbstractState
open class State : AbstractState()

sealed class AbstractMsg
open class Msg : AbstractMsg()
object Idle : Msg()
object Init : Msg()
class ErrorMsg(val err: Throwable, val cmd: Cmd) : Msg()


sealed class AbstractCmd
open class Cmd : AbstractCmd()
data class OneShotCmd(val msg: Msg) : Cmd()
object None : Cmd()
data class BatchCmd(val cmds: List<Cmd>) : Cmd()

interface Component<S : State> {

    fun update(msg: Msg, state: S): Pair<S, Cmd>

    fun render(state: S)

    fun call(cmd: Cmd): Single<Msg>

}


class Program<S : State>(val outputScheduler: Scheduler) {

    private val msgRelay: BehaviorRelay<Msg> = BehaviorRelay.create()
    private var msgQueue = ArrayDeque<Msg>()
    private var disposableMap: MutableMap<String, Disposable> = mutableMapOf()
    lateinit private var state: S
    private lateinit var component: Component<S>
    private var lock: Boolean = false

    fun init(initialState: S, component: Component<S>): Disposable {
        this.component = component
        this.state = initialState
        return msgRelay
            .observeOn(outputScheduler)
            .map { msg ->
                Timber.d("elm reduce msg:${msg.javaClass.simpleName} ")
                val updateResult = component.update(msg, this.state)
                val newState = updateResult.first
                this.state = newState
                if (msgQueue.size > 0) {
                    msgQueue.removeFirst()
                    Timber.d("elm remove from queue:${msg.javaClass.simpleName}")
                }

                lock = false
                component.render(newState)
                loop()
                return@map updateResult
            }
            .filter { (_, cmd) -> cmd !is None }
            .observeOn(Schedulers.io())
            .flatMap { (_, cmd) ->
                Timber.d("elm call cmd:$cmd")
                call(cmd)
            }
            .observeOn(outputScheduler)
            .subscribe({ msg ->
                when (msg) {
                    is Idle -> {
                    }
                    else -> msgQueue.addLast(msg)
                }

                loop()
            })
    }

    fun call(cmd: Cmd): Observable<Msg> {
        return when (cmd) {
            is BatchCmd ->
                Observable.merge(cmd.cmds.map {
                    cmdCall(it)
                })
            else -> cmdCall(cmd)
        }
    }

    private fun cmdCall(cmd: Cmd): Observable<Msg> {
        return when (cmd) {
            is OneShotCmd -> Observable.just(cmd.msg)
            else -> component.call(cmd)
                .onErrorResumeNext { err -> Single.just(ErrorMsg(err, cmd)) }
                .toObservable()
        }
    }

    fun getState(): S {
        return state
    }

    private fun loop() {
        Timber.d("elm loop queue size:${msgQueue.size}")
        msgQueue.forEach { Timber.d("elm in queue:${it.javaClass.simpleName}") }
        if (!lock && msgQueue.size > 0) {
            lock = true
            Timber.d("elm accept from loop ${msgQueue.first}")
            msgRelay.accept(msgQueue.first)
        }
    }

    fun accept(msg: Msg) {
        msgQueue.addLast(msg)
        Timber.d("elm add msg: ${msg.javaClass.simpleName} queue size:${msgQueue.size} ")
        msgQueue.forEach { Timber.d("elm accept in queue:${it.javaClass.simpleName}") }
        if (!lock && msgQueue.size == 1) {
            lock = true
            Timber.d("elm accept event:${msg.javaClass.simpleName}")
            msgRelay.accept(msgQueue.first)
        }
    }

    fun render() {
        component.render(this.state)
    }

    fun destroy() {
        disposableMap.forEach { (_, disposable) -> if (!disposable.isDisposed) disposable.dispose() }
    }

}

inline fun inView(crossinline operations : () -> Unit) : Single<Msg> {
    return Single.fromCallable {
        operations()
    }.subscribeOn(AndroidSchedulers.mainThread()).map { Idle }
}
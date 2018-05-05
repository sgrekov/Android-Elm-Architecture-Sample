package com.sample.android.elm.login.mobius

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

sealed class LoginEffect
object GetSavedUserEffect : LoginEffect()
data class SaveUserCredentialsEffect(val login: String, val pass: String) : LoginEffect()
data class LoginActionEffect(val login: String, val pass: String) : LoginEffect()
object GoToMainEffect : LoginEffect()


sealed class LoginEvent
object LoginInit : LoginEvent()
data class UserCredentialsLoadedEvent(val login: String, val pass: String, val err: Throwable? = null) : LoginEvent()
class UserCredentialsSavedEvent : LoginEvent()
data class LoginInputEvent(val login: String) : LoginEvent()
data class PassInputEvent(val pass: String) : LoginEvent()
data class IsSaveCredentialsEvent(val checked: Boolean) : LoginEvent()
data class LoginResponseEvent(val logged: Boolean, val err: Throwable? = null) : LoginEvent()
class LoginClickEvent : LoginEvent()
data class LoginErrorEvent(val err: Throwable) : LoginEvent()
object IdleEvent : LoginEvent()


data class LoginModel(
    val login: String = "",
    val loginError: String? = null,
    val pass: String = "",
    val passError: String? = null,
    val saveUser: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val btnEnabled: Boolean = false
)

inline fun inView(crossinline operations : () -> Unit) : Observable<LoginEvent> {
    return Single.fromCallable {
        operations()
    }.subscribeOn(AndroidSchedulers.mainThread()).map { IdleEvent as LoginEvent }.toObservable()
}
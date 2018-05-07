package com.sample.android.mobius.domain.login

sealed class LoginEffect
object GetSavedUserEffect : LoginEffect()
data class SaveUserCredentialsEffect(val login: String, val pass: String) : LoginEffect()
data class LoginRequestEffect(val login: String, val pass: String) : LoginEffect()
object GoToMainEffect : LoginEffect()


sealed class LoginEvent
object LoginInit : LoginEvent()
data class UserCredentialsLoadedEvent(val login: String, val pass: String) : LoginEvent()
data class UserCredentialsErrorEvent(val err: Throwable? = null) : LoginEvent()
object UserCredentialsSavedEvent : LoginEvent()
data class LoginInputEvent(val login: String) : LoginEvent()
data class PassInputEvent(val pass: String) : LoginEvent()
data class IsSaveCredentialsEvent(val checked: Boolean) : LoginEvent()
data class LoginResponseEvent(val logged: Boolean) : LoginEvent()
data class LoginResponseErrorEvent(val err: Throwable? = null) : LoginEvent()
object LoginClickEvent : LoginEvent()


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
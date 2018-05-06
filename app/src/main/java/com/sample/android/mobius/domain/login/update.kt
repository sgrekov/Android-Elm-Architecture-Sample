package com.sample.android.mobius.domain.login

import com.spotify.mobius.Effects.effects
import com.spotify.mobius.Next
import com.spotify.mobius.Update


class LoginUpdate : Update<LoginModel, LoginEvent, LoginEffect> {

    override fun update(model: LoginModel, event: LoginEvent): Next<LoginModel, LoginEffect> {
        return when (event) {
            is LoginInit -> Next.next(model.copy(isLoading = true), effects(GetSavedUserEffect))
            is UserCredentialsLoadedEvent -> Next.next(
                model.copy(login = event.login, pass = event.pass),
                effects(LoginRequestEffect(event.login, event.pass))
            )
            is UserCredentialsErrorEvent -> Next.next(model.copy(isLoading = false))
            is LoginResponseEvent -> {
                return when {
                    event.err != null -> Next.next(model.copy(isLoading = false, error = event.err.message))
                    model.saveUser -> Next.dispatch(
                        effects(
                            SaveUserCredentialsEffect(
                                model.login,
                                model.pass
                            )
                        )
                    )
                    else -> Next.dispatch(effects(GoToMainEffect))
                }
            }
            is LoginResponseErrorEvent -> Next.next(
                model.copy(isLoading = false, error = event.err?.message)
            )
            is UserCredentialsSavedEvent -> Next.dispatch(effects(GoToMainEffect))
            is IsSaveCredentialsEvent -> Next.next(model.copy(saveUser = event.checked))
            is LoginInputEvent -> {
                if (!validateLogin(event.login))
                    Next.next(model.copy(login = event.login, btnEnabled = false))
                else
                    Next.next(model.copy(login = event.login, loginError = null, btnEnabled = validatePass(model.pass)))
            }
            is PassInputEvent -> {
                if (!validatePass(event.pass))
                    Next.next(model.copy(pass = event.pass, btnEnabled = false))
                else
                    Next.next(model.copy(pass = event.pass, btnEnabled = validateLogin(model.login)))
            }
            is LoginClickEvent -> {
                if (checkLogin(model.login)) {
                    return Next.next(model.copy(loginError = "Login is not valid"))
                }
                if (checkPass(model.pass)) {
                    return Next.next(model.copy(passError = "Password is not valid"))
                }
                return Next.next(
                    model.copy(isLoading = true, error = null),
                    effects(LoginRequestEffect(model.login, model.pass))
                )
            }
            IdleEvent -> Next.noChange()
        }
    }

}


internal fun validatePass(pass: CharSequence): Boolean {
    return pass.length > 4
}

internal fun validateLogin(login: CharSequence): Boolean {
    return login.length > 3
}

internal fun checkPass(pass: CharSequence): Boolean {
    return (pass.startsWith("42") || pass == "qwerty")
}

internal fun checkLogin(login: CharSequence): Boolean {
    return (login.startsWith("42") || login == "admin")
}
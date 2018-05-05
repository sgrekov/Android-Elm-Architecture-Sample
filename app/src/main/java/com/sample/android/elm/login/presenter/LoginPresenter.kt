package com.sample.android.elm.login.presenter

import com.sample.android.elm.Cmd
import com.sample.android.elm.Component
import com.sample.android.elm.ErrorMsg
import com.sample.android.elm.Idle
import com.sample.android.elm.Init
import com.sample.android.elm.Msg
import com.sample.android.elm.None
import com.sample.android.elm.Program
import com.sample.android.elm.State
import com.sample.android.elm.data.IApiService
import com.sample.android.elm.data.IAppPrefs
import com.sample.android.elm.login.view.ILoginView
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import org.eclipse.egit.github.core.client.RequestException
import timber.log.Timber

class LoginPresenter(private val loginView: ILoginView,
                     private val program: Program<LoginState>,
                     private val appPrefs: IAppPrefs,
                     private val apiService: IApiService,
                     private val scheduler: Scheduler) : Component<LoginPresenter.LoginState> {

    data class LoginState(val login: String = "",
                          val loginError: String? = null,
                          val pass: String = "",
                          val passError: String? = null,
                          val saveUser: Boolean = false,
                          val isLoading: Boolean = true,
                          val error: String? = null,
                          val btnEnabled: Boolean = false,
                          val isLogged: Boolean = false) : State()

    class GetSavedUserCmd : Cmd()
    data class SaveUserCredentialsCmd(val login: String, val pass: String) : Cmd()
    data class LoginCmd(val login: String, val pass: String) : Cmd()

    data class UserCredentialsLoadedMsg(val login: String, val pass: String) : Msg()
    class UserCredentialsSavedMsg : Msg()
    data class LoginInputMsg(val login: String) : Msg()
    data class PassInputMsg(val pass: String) : Msg()
    data class IsSaveCredentialsMsg(val checked: Boolean) : Msg()
    data class LoginResponseMsg(val logged: Boolean) : Msg()
    class LoginClickMsg : Msg()

    var programDisposable: Disposable

    init {
        programDisposable = program.init(LoginState(), this)
    }

    fun init() {
        program.accept(Init)
    }


    override fun update(msg: Msg, state: LoginState): Pair<LoginState, Cmd> {
        return when (msg) {
            is Init -> {
                Pair(state.copy(isLoading = true), GetSavedUserCmd())
            }
            is UserCredentialsLoadedMsg -> {
                Pair(state.copy(login = msg.login, pass = msg.pass), LoginCmd(msg.login, msg.pass))
            }
            is LoginResponseMsg -> {
                if (state.saveUser) {
                    Pair(state, SaveUserCredentialsCmd(state.login, state.pass))
                } else {
                    Pair(state.copy(isLogged = true), None)
                }
            }
            is UserCredentialsSavedMsg -> {
                Pair(state.copy(isLogged = true), None)
            }
            is IsSaveCredentialsMsg -> {
                Pair(state.copy(saveUser = msg.checked), None)
            }
            is LoginInputMsg -> {
                return if (!validateLogin(msg.login))
                    Pair(state.copy(login = msg.login, btnEnabled = false), None)
                else
                    Pair(state.copy(login = msg.login, loginError = null, btnEnabled = validatePass(state.pass)), None)
            }
            is PassInputMsg -> {
                return if (!validatePass(msg.pass))
                    Pair(state.copy(pass = msg.pass, btnEnabled = false), None)
                else
                    Pair(state.copy(pass = msg.pass, btnEnabled = validateLogin(state.login)), None)
            }
            is LoginClickMsg -> {
                if (checkLogin(state.login)) {
                    return Pair(state.copy(loginError = "Login is not valid"), None)
                }
                if (checkPass(state.pass)) {
                    return Pair(state.copy(passError = "Password is not valid"), None)
                }
                Pair(state.copy(isLoading = true, error = null), LoginCmd(state.login, state.pass))
            }
            is ErrorMsg -> {
                return when (msg.cmd) {
                    is GetSavedUserCmd -> Pair(state.copy(isLoading = false), None)
                    is LoginCmd -> {
                        if (msg.err is RequestException) {
                            return Pair(state.copy(isLoading = false, error = msg.err.error.message), None)
                        }
                        return Pair(state.copy(isLoading = false, error = "Error while login"), None)
                    }
                    else -> {
                        Timber.e(msg.err)
                        Pair(state, None)
                    }
                }
            }
            else -> Pair(state, None)
        }
    }

    override fun render(state: LoginState) {
        state.apply {
            if (isLogged) {
                loginView.hideKeyboard()
                loginView.goToMainScreen()
                return
            }
            if (isLoading) {
                loginView.showProgress()
            } else {
                loginView.hideProgress()
            }

            if (btnEnabled) {
                loginView.enableLoginBtn()
            } else {
                loginView.disableLoginBtn()
            }

            error?.let {
                loginView.showError()
                loginView.error(it)
            } ?: loginView.hideError()

            loginError?.let {
                loginView.showLoginError(it)
            } ?: loginView.hideLoginError()

            passError?.let {
                loginView.showPasswordError(it)
            } ?: loginView.hidePasswordError()
        }
    }

    fun render(){
        program.render()
    }

    override fun call(cmd: Cmd): Single<Msg> {
        return when (cmd) {
            is GetSavedUserCmd -> appPrefs.getUserSavedCredentials()
                    .map { (login, pass) -> UserCredentialsLoadedMsg(login, pass) }
            is SaveUserCredentialsCmd -> appPrefs.saveUserSavedCredentials(cmd.login, cmd.pass)
                    .map { saved -> UserCredentialsSavedMsg() }
            is LoginCmd -> apiService.login(cmd.login, cmd.pass)
                    .map { logged -> LoginResponseMsg(logged) }
            else -> Single.just(Idle)
        }
    }

    fun loginBtnClick() {
        program.accept(LoginClickMsg())
    }

    fun onSaveCredentialsCheck(checked: Boolean) {
        program.accept(IsSaveCredentialsMsg(checked))
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

    fun addLoginInput(logintextViewText: Observable<CharSequence>) : Disposable {
        return logintextViewText.skip(1).subscribe({ login ->
            program.accept(LoginInputMsg(login.toString()))
        })
    }

    fun addPasswordInput(passValueObservable: Observable<CharSequence>) : Disposable {
        return passValueObservable.skip(1).subscribe({ pass ->
            program.accept(PassInputMsg(pass.toString()))
        })
    }

    fun destroy() {
        programDisposable.dispose()
    }
}

package com.sample.android.elm.login.view

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import com.androidjacoco.sample.R
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sample.android.elm.AndroidNavigator
import com.sample.android.elm.Navigator
import com.sample.android.elm.SampleApp
import com.sample.android.elm.data.AppPrefs
import com.sample.android.elm.data.GitHubService
import com.sample.android.elm.login.mobius.GetSavedUserEffect
import com.sample.android.elm.login.mobius.GoToMainEffect
import com.sample.android.elm.login.mobius.IdleEvent
import com.sample.android.elm.login.mobius.IsSaveCredentialsEvent
import com.sample.android.elm.login.mobius.LoginActionEffect
import com.sample.android.elm.login.mobius.LoginClickEvent
import com.sample.android.elm.login.mobius.LoginEffect
import com.sample.android.elm.login.mobius.LoginErrorEvent
import com.sample.android.elm.login.mobius.LoginEvent
import com.sample.android.elm.login.mobius.LoginInit
import com.sample.android.elm.login.mobius.LoginInputEvent
import com.sample.android.elm.login.mobius.LoginModel
import com.sample.android.elm.login.mobius.LoginResponseEvent
import com.sample.android.elm.login.mobius.PassInputEvent
import com.sample.android.elm.login.mobius.SaveUserCredentialsEffect
import com.sample.android.elm.login.mobius.UserCredentialsLoadedEvent
import com.sample.android.elm.login.mobius.UserCredentialsSavedEvent
import com.sample.android.elm.login.mobius.inView
import com.spotify.mobius.First
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Next
import com.spotify.mobius.Update
import com.spotify.mobius.android.AndroidLogger
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.rx2.RxConnectables
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers


class LoginFragment : Fragment(), ILoginView,
    Update<LoginModel, LoginEvent, LoginEffect>,
    ObservableTransformer<LoginEffect, LoginEvent> {

    lateinit var prefs: AppPrefs
    lateinit var api: GitHubService
    lateinit var navigator: Navigator

    var loopFactory: MobiusLoop.Factory<LoginModel, LoginEvent, LoginEffect> =
        RxMobius
            .loop(this, this)
            .init {
                First.first(LoginModel(), setOf(GetSavedUserEffect))
            }
            .logger(AndroidLogger.tag<LoginModel, LoginEvent, LoginEffect>("my_app"))

    private val controller: MobiusLoop.Controller<LoginModel, LoginEvent> =
        MobiusAndroid.controller(loopFactory, LoginModel())

    lateinit var loginInput: TextInputLayout
    lateinit var loginText: TextInputEditText
    lateinit var passwordInput: TextInputLayout
    lateinit var passwordText: TextInputEditText
    lateinit var loginBtn: Button
    lateinit var errorTxt: TextView
    lateinit var loginProgress: ProgressBar
    lateinit var saveCredentialsCb: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        prefs = AppPrefs(activity?.getPreferences(Context.MODE_PRIVATE)!!)
        api = (activity.application as SampleApp).service
        navigator = AndroidNavigator(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater?, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater!!.inflate(R.layout.fragment_login, container, false)
        loginInput = view.findViewById(R.id.login_til) as TextInputLayout
        loginText = view.findViewById(R.id.login) as TextInputEditText
        passwordInput = view.findViewById(R.id.password_til) as TextInputLayout
        passwordText = view.findViewById(R.id.password) as TextInputEditText
        loginBtn = view.findViewById(R.id.login_btn) as Button
        errorTxt = view.findViewById(R.id.error) as TextView
        loginProgress = view.findViewById(R.id.login_progress) as ProgressBar
        saveCredentialsCb = view.findViewById(R.id.save_credentials_cb) as CheckBox
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        controller.connect(RxConnectables.fromTransformer(this::connectViews))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        controller.disconnect()
    }

    override fun onResume() {
        super.onResume()
        controller.start()
    }

    override fun onPause() {
        super.onPause()
        controller.stop()
    }

    override fun update(state: LoginModel, event: LoginEvent): Next<LoginModel, LoginEffect> {
        return when (event) {
            is LoginInit -> Next.next(state.copy(isLoading = true), setOf(GetSavedUserEffect))
            is UserCredentialsLoadedEvent ->
                return if (event.err == null) {
                    Next.next(
                        state.copy(login = event.login, pass = event.pass),
                        setOf(LoginActionEffect(event.login, event.pass))
                    )
                } else {
                    Next.next(
                        state.copy(isLoading = false)
                    )
                }
            is LoginResponseEvent -> {
                return when {
                    event.err != null -> Next.next(state.copy(isLoading = false, error = event.err.message))
                    state.saveUser -> Next.next(state, setOf(SaveUserCredentialsEffect(state.login, state.pass)))
                    else -> Next.next(state, setOf(GoToMainEffect))
                }
            }
            is UserCredentialsSavedEvent -> Next.next(state, setOf(GoToMainEffect))
            is IsSaveCredentialsEvent -> Next.next(state.copy(saveUser = event.checked))
            is LoginInputEvent -> {
                if (!validateLogin(event.login))
                    Next.next(state.copy(login = event.login, btnEnabled = false))
                else
                    Next.next(state.copy(login = event.login, loginError = null, btnEnabled = validatePass(state.pass)))
            }
            is PassInputEvent -> {
                if (!validatePass(event.pass))
                    Next.next(state.copy(pass = event.pass, btnEnabled = false))
                else
                    Next.next(state.copy(pass = event.pass, btnEnabled = validateLogin(state.login)))
            }
            is LoginClickEvent -> {
                if (checkLogin(state.login)) {
                    return Next.next(state.copy(loginError = "Login is not valid"))
                }
                if (checkPass(state.pass)) {
                    return Next.next(state.copy(passError = "Password is not valid"))
                }
                return Next.next(
                    state.copy(isLoading = true, error = null),
                    setOf(LoginActionEffect(state.login, state.pass))
                )
            }
            is LoginErrorEvent -> {
                return Next.next(
                    state.copy(isLoading = false, error = event.err.message)
                )
            }
            IdleEvent -> Next.noChange()
        }
    }

    override fun apply(upstream: Observable<LoginEffect>): ObservableSource<LoginEvent> {
        return upstream.flatMap { effect ->
            return@flatMap when (effect) {
                is GetSavedUserEffect -> prefs.getUserSavedCredentials()
                    .map { (login, pass) -> UserCredentialsLoadedEvent(login, pass) }
                    .onErrorReturn { return@onErrorReturn UserCredentialsLoadedEvent("", "", err = it) }.toObservable()
                is SaveUserCredentialsEffect -> prefs.saveUserSavedCredentials(effect.login, effect.pass)
                    .map { saved -> UserCredentialsSavedEvent() }.toObservable()
                is LoginActionEffect -> api.login(effect.login, effect.pass)
                    .map { logged -> LoginResponseEvent(logged) }
                    .onErrorReturn { LoginResponseEvent(false, it) }
                    .toObservable()
                is GoToMainEffect ->
                    inView {
                        navigator.goToMainScreen()
                    }
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


    fun connectViews(models: Observable<LoginModel>): Observable<LoginEvent> {
        val disposable = models
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { render(it) }

        val loginBtnClick = RxView.clicks(loginBtn)
            .map { LoginClickEvent() as LoginEvent }
        val loginText = RxTextView.textChanges(loginText)
            .map { LoginInputEvent(it.toString()) as LoginEvent }
        val passText = RxTextView.textChanges(passwordText)
            .map { PassInputEvent(it.toString()) as LoginEvent }
        val saveCreds = RxCompoundButton.checkedChanges(saveCredentialsCb)
            .map { IsSaveCredentialsEvent(it) }

        return Observable
            .merge(listOf(loginBtnClick, loginText, passText, saveCreds))
            .doOnDispose(disposable::dispose)
    }

    fun render(state: LoginModel) {
        state.apply {
            setProgress(isLoading)
            setEnableLoginBtn(btnEnabled)
            setError(error)
            showLoginError(loginError)
            showPasswordError(passError)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun setProgress(show: Boolean) {
        loginProgress.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showPasswordError(errorText: String?) {
        errorText?.let {
            passwordInput.error = errorText
        } ?: run {
            passwordInput.error = ""
        }
    }

    override fun showLoginError(errorText: String?) {
        errorText?.let {
            loginInput.error = errorText
        } ?: run {
            loginInput.error = ""
        }
    }

    override fun setError(error: String?) {
        error?.let {
            errorTxt.visibility = View.VISIBLE
            errorTxt.text = error
        } ?: run {
            errorTxt.visibility = View.GONE
        }
    }

    override fun hideKeyboard() {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?

        imm?.hideSoftInputFromWindow(loginText.windowToken, 0)
    }

    override fun setEnableLoginBtn(enabled: Boolean) {
        loginBtn.isEnabled = enabled
    }


}

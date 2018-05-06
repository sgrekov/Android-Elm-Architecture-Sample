package com.sample.android.mobius.login.view

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
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sample.android.mobius.AndroidNavigator
import com.sample.android.mobius.Navigator
import com.sample.android.mobius.R
import com.sample.android.mobius.SampleApp
import com.sample.android.mobius.data.AppPrefs
import com.sample.android.mobius.data.GitHubService
import com.sample.android.mobius.domain.login.GetSavedUserEffect
import com.sample.android.mobius.domain.login.GoToMainEffect
import com.sample.android.mobius.domain.login.IsSaveCredentialsEvent
import com.sample.android.mobius.domain.login.LoginClickEvent
import com.sample.android.mobius.domain.login.LoginEffect
import com.sample.android.mobius.domain.login.LoginEvent
import com.sample.android.mobius.domain.login.LoginInputEvent
import com.sample.android.mobius.domain.login.LoginModel
import com.sample.android.mobius.domain.login.LoginRequestEffect
import com.sample.android.mobius.domain.login.LoginResponseErrorEvent
import com.sample.android.mobius.domain.login.LoginResponseEvent
import com.sample.android.mobius.domain.login.LoginUpdate
import com.sample.android.mobius.domain.login.PassInputEvent
import com.sample.android.mobius.domain.login.SaveUserCredentialsEffect
import com.sample.android.mobius.domain.login.UserCredentialsErrorEvent
import com.sample.android.mobius.domain.login.UserCredentialsLoadedEvent
import com.sample.android.mobius.domain.login.UserCredentialsSavedEvent
import com.spotify.mobius.First
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.android.AndroidLogger
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.rx2.RxConnectables
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers


class LoginFragment : Fragment(), ILoginView {

    lateinit var prefs: AppPrefs
    lateinit var api: GitHubService
    lateinit var navigator: Navigator

    @BindView(R.id.login_til) lateinit var loginInput: TextInputLayout
    @BindView(R.id.login) lateinit var loginText: TextInputEditText
    @BindView(R.id.password_til) lateinit var passwordInput: TextInputLayout
    @BindView(R.id.password) lateinit var passwordText: TextInputEditText
    @BindView(R.id.login_btn) lateinit var loginBtn: Button
    @BindView(R.id.error) lateinit var errorTxt: TextView
    @BindView(R.id.login_progress) lateinit var loginProgress: ProgressBar
    @BindView(R.id.save_credentials_cb) lateinit var saveCredentialsCb: CheckBox

    lateinit var unbinder: Unbinder

    var rxEffectHandler =
        RxMobius.subtypeEffectHandler<LoginEffect, LoginEvent>()
            .add(GetSavedUserEffect::class.java, this::handleGetUserSavedCredentials)
            .add(SaveUserCredentialsEffect::class.java, this::handleSaveUserSavedCredentials)
            .add(LoginRequestEffect::class.java, this::handleLoginRequest)
            .add(GoToMainEffect::class.java, this::handleNavigateToMainScreen, AndroidSchedulers.mainThread())
            .build()

    var loopFactory: MobiusLoop.Factory<LoginModel, LoginEvent, LoginEffect> =
        RxMobius
            .loop(LoginUpdate(), rxEffectHandler)
            .init {
                First.first(LoginModel(), setOf(GetSavedUserEffect))
            }
            .logger(AndroidLogger.tag<LoginModel, LoginEvent, LoginEffect>("my_app"))

    private val controller: MobiusLoop.Controller<LoginModel, LoginEvent> =
        MobiusAndroid.controller(loopFactory, LoginModel())


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
        unbinder = ButterKnife.bind(this, view)
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

    fun connectViews(models: Observable<LoginModel>): Observable<LoginEvent> {
        val disposable = models
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { render(it) }

        val loginBtnClick = RxView.clicks(loginBtn)
            .map { LoginClickEvent as LoginEvent }
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

    fun handleGetUserSavedCredentials(
        request: Observable<GetSavedUserEffect>
    ): Observable<LoginEvent> {

        return prefs.getUserSavedCredentials()
            .map { (login, pass) ->
                UserCredentialsLoadedEvent(
                    login,
                    pass
                ) as LoginEvent
            }
            .toObservable()
            .onErrorReturn { return@onErrorReturn UserCredentialsErrorEvent(err = it) }
    }

    fun handleSaveUserSavedCredentials(
        request: Observable<SaveUserCredentialsEffect>
    ): Observable<LoginEvent> {

        return request.flatMap { effect ->
            prefs.saveUserSavedCredentials(effect.login, effect.pass)
                .map { saved -> UserCredentialsSavedEvent }.toObservable()
        }
    }

    fun handleLoginRequest(
        request: Observable<LoginRequestEffect>
    ): Observable<LoginEvent> {

        return request.flatMap { effect ->
            api.login(effect.login, effect.pass)
                .map { logged -> LoginResponseEvent(logged) as LoginEvent }
                .onErrorReturn { LoginResponseErrorEvent(it) }
                .toObservable()
        }
    }

    fun handleNavigateToMainScreen() {
        navigator.goToMainScreen()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbinder.unbind()
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

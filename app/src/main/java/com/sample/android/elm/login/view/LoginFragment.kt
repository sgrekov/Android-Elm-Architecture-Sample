package com.sample.android.elm.login.view

import android.content.Context
import android.content.Intent
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
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sample.android.elm.Program
import com.sample.android.elm.SampleApp
import com.sample.android.elm.data.AppPrefs
import com.sample.android.elm.login.presenter.LoginPresenter
import com.sample.android.elm.main.view.MainActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class LoginFragment : Fragment(), ILoginView {

    var viewDisposables: CompositeDisposable = CompositeDisposable()

    lateinit var presenter: LoginPresenter
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
        presenter = LoginPresenter(this,
                Program(AndroidSchedulers.mainThread()),
                AppPrefs(context.getSharedPreferences("prefs", Context.MODE_PRIVATE)),
                (activity.application as SampleApp).service,
                AndroidSchedulers.mainThread())
        presenter.init()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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
        viewDisposables.add(presenter.addLoginInput(RxTextView.textChanges(loginText)))
        viewDisposables.add(presenter.addPasswordInput(RxTextView.textChanges(passwordText)))
        loginBtn.setOnClickListener { presenter.loginBtnClick() }
        saveCredentialsCb.setOnCheckedChangeListener { buttonView, isChecked ->
            hideKeyboard()
            presenter.onSaveCredentialsCheck(isChecked)
        }

        presenter.render()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (!viewDisposables.isDisposed) {
            viewDisposables.dispose()
            viewDisposables = CompositeDisposable()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }


    override fun error(error: String) {
        errorTxt.text = error
    }

    override fun showProgress() {
        loginProgress.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        loginProgress.visibility = View.GONE
    }


    override fun showPasswordError(error: String) {
        passwordInput.error = error
    }

    override fun showLoginError(error: String) {
        loginInput.error = error
    }

    override fun showError() {
        errorTxt.visibility = View.VISIBLE
    }

    override fun hideError() {
        errorTxt.visibility = View.GONE
    }

    override fun hideLoginError() {
        loginInput.error = ""
    }

    override fun hidePasswordError() {
        passwordInput.error = ""
    }

    override fun hideKeyboard() {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?

        imm?.hideSoftInputFromWindow(loginText.windowToken, 0)
    }

    override fun disableLoginBtn() {
        loginBtn.isEnabled = false
    }

    override fun enableLoginBtn() {
        loginBtn.isEnabled = true
    }

    override fun goToMainScreen() {
        val i = Intent(activity, MainActivity::class.java)
        startActivity(i)
        activity.finish()
    }

}

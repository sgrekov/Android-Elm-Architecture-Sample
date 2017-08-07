package com.sample.android.elm.login.presenter

import com.sample.android.elm.Init
import com.sample.android.elm.None
import com.sample.android.elm.Program
import com.sample.android.elm.data.IApiService
import com.sample.android.elm.data.IAppPrefs
import com.sample.android.elm.login.view.ILoginView
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*

class LoginPresenterTest {

    lateinit var presenter: LoginPresenter
    lateinit var view: ILoginView
    lateinit var loginService: IApiService
    lateinit var prefs: IAppPrefs
    lateinit var program: Program

    @Before
    fun setUp() {
        view = mock(ILoginView::class.java)
        loginService = mock(IApiService::class.java)
        prefs = mock(IAppPrefs::class.java)
        program = Program(Schedulers.trampoline())
        presenter = LoginPresenter(view, program, prefs, loginService, Schedulers.trampoline())
    }

    @Test
    fun initWithSavedLogin() {
        //login screen init and look for saved credentials in preferences
        var initState = LoginPresenter.LoginState()
        //update
        val (searchForLoginState, searchForLoginCmd) = presenter.update(Init(), initState)

        assertEquals(initState.copy(isLoading = true), searchForLoginState)
        assertThat(searchForLoginCmd, instanceOf(LoginPresenter.GetSavedUserCmd::class.java))

        //render
        presenter.render(searchForLoginState)
        verify(view).showProgress()
        verify(view).disableLoginBtn()
        verify(view).hideLoginError()
        verify(view).hidePasswordError()
        verify(view).hideError()
        verifyNoMoreInteractions(view)

        Mockito.`when`(prefs.getUserSavedCredentials()).thenReturn(Single.just(Pair("login", "password")))
        //call
        val loadedCredentialsMsg = presenter.call(searchForLoginCmd)

        //credentials loaded and start auth http call
        //update
        val (startAuthState, startAuthCmd) = presenter.update(loadedCredentialsMsg.blockingGet(), searchForLoginState)
        assertEquals((searchForLoginState as LoginPresenter.LoginState).copy(login = "login", pass = "password"), startAuthState)
        assertThat(startAuthCmd, instanceOf(LoginPresenter.LoginCmd::class.java))
        assertEquals("login", (startAuthCmd as LoginPresenter.LoginCmd).login)
        assertEquals("password", startAuthCmd.pass)

        Mockito.reset(view)
        //render
        presenter.render(startAuthState)
        verify(view).showProgress()
        verify(view).disableLoginBtn()
        verify(view).hideLoginError()
        verify(view).hidePasswordError()
        verify(view).hideError()
        verifyNoMoreInteractions(view)

        Mockito.`when`(loginService.login("login", "password")).thenReturn(Single.just(true))
        //call
        val authOkMsg = presenter.call(startAuthCmd)

        //auth OK, go to main screen
        //update
        val (loggedState, noneCmd) = presenter.update(authOkMsg.blockingGet(), startAuthState)
        assertThat(noneCmd, instanceOf(None::class.java))

        Mockito.reset(view)
        //render
        presenter.render(loggedState)
        verify(view).goToMainScreen()
        verify(view).hideKeyboard()
        verifyNoMoreInteractions(view)

    }


}

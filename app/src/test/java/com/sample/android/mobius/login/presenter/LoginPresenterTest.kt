package com.sample.android.mobius.login.presenter

import com.sample.android.mobius.domain.login.GoToMainEffect
import com.sample.android.mobius.domain.login.LoginClickEvent
import com.sample.android.mobius.domain.login.LoginEffect
import com.sample.android.mobius.domain.login.LoginInputEvent
import com.sample.android.mobius.domain.login.LoginModel
import com.sample.android.mobius.domain.login.LoginRequestEffect
import com.sample.android.mobius.domain.login.LoginResponseEvent
import com.sample.android.mobius.domain.login.LoginUpdate
import com.sample.android.mobius.domain.login.PassInputEvent
import com.sample.android.mobius.domain.login.UserCredentialsErrorEvent
import com.sample.android.mobius.domain.login.UserCredentialsLoadedEvent
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class LoginPresenterTest {

    val spec = UpdateSpec(LoginUpdate())

    @Test
    fun initWithSavedLogin_StartLoginRequest() {
        //login screen init and look for saved credentials in preferences
        var initState = LoginModel()
        //update
        spec.given(initState).`when`(
            UserCredentialsLoadedEvent("login", "pass")
        ).then(
            assertThatNext(
                hasModel(initState.copy(login = "login", pass = "pass")),
                hasEffects(LoginRequestEffect("login", "pass") as LoginEffect)
            )
        )
    }

    @Test
    fun initWithSavedLogin_NoSavedCredentialsInPrefs() {
        //login screen init and look for saved credentials in preferences
        var initState = LoginModel()
        //update
        spec.given(initState).`when`(
            UserCredentialsErrorEvent(NoSuchElementException())
        ).then(
            assertThatNext(
                hasModel(initState.copy(isLoading = false)),
                hasNoEffects()
            )
        )
    }

    @Test
    fun userStartTyping() {
        var initState = LoginModel(isLoading = false)
        //update
        spec.given(initState).`when`(
            LoginInputEvent("l")
        ).then(
            assertThatNext(
                hasModel(initState.copy(login = "l", btnEnabled = false)),
                hasNoEffects()
            )
        )

        spec.given(initState).`when`(
            LoginInputEvent("lo")
        ).then(
            assertThatNext(
                hasModel(initState.copy(login = "lo", btnEnabled = false)),
                hasNoEffects()
            )
        )

        spec.given(initState).`when`(
            LoginInputEvent("login")
        ).then(
            assertThatNext(
                hasModel(initState.copy(login = "login", btnEnabled = false)),
                hasNoEffects()
            )
        )

        val stateAfterLogin = initState.copy(login = "login")
        spec.given(stateAfterLogin).`when`(
            PassInputEvent("pass")
        ).then(
            assertThatNext(
                hasModel(stateAfterLogin.copy(pass = "pass", btnEnabled = false)),
                hasNoEffects()
            )
        )

        spec.given(stateAfterLogin).`when`(
            PassInputEvent("passwo")
        ).then(
            assertThatNext(
                hasModel(stateAfterLogin.copy(pass = "passwo", btnEnabled = true)),
                hasNoEffects()
            )
        )
    }

    @Test
    fun startLoginRequest() {
        val loginPassState = LoginModel().copy(login = "login", pass = "passwo", btnEnabled = true, isLoading = false)
        spec.given(loginPassState).`when`(
            LoginClickEvent
        ).then(
            assertThatNext(
                hasModel(loginPassState.copy(isLoading = true)),
                hasEffects(LoginRequestEffect(login = "login", pass = "passwo") as LoginEffect)
            )
        )

        spec.given(loginPassState.copy(isLoading = true)).`when`(
            LoginResponseEvent(logged = true)
        ).then(
            assertThatNext(
                hasEffects(GoToMainEffect as LoginEffect)
            )
        )
    }


}

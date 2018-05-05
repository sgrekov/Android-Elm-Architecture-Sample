package com.sample.android.elm.login.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.androidjacoco.sample.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_main)

        if (supportFragmentManager.findFragmentByTag("TAG") == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.login_fragment, LoginFragment(), "TAG")
                .commit()
        }

    }

}

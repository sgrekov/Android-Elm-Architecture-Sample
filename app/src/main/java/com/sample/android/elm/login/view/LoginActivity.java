package com.sample.android.elm.login.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.androidjacoco.sample.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);

        if (getSupportFragmentManager().findFragmentByTag("TAG") == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.login_fragment, new LoginFragment(), "TAG")
                    .commit();
        }

    }

}

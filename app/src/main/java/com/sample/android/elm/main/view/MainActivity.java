package com.sample.android.elm.main.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import com.androidjacoco.sample.R;
import com.sample.android.elm.main.presenter.MainPresenter;

public class MainActivity extends AppCompatActivity implements IMainView {

    public static final String USERNAME_KEY = "username";

    MainPresenter presenter;
    TextView welcomeTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        welcomeTv = (TextView)findViewById(R.id.welcome_tv);

        presenter = new MainPresenter(this, getIntent().getStringExtra(USERNAME_KEY));
        presenter.showWelcome();
    }


    @Override
    public void showUserWelcome(String username) {
        welcomeTv.setText(username);
    }
}

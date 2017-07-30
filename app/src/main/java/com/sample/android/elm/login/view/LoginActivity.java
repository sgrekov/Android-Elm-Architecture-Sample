package com.sample.android.elm.login.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.androidjacoco.sample.R;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.sample.android.elm.ElmProgram;
import com.sample.android.elm.SampleApp;
import com.sample.android.elm.data.AppPrefs;
import com.sample.android.elm.login.presenter.LoginPresenter;
import com.sample.android.elm.main.view.MainActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import org.jetbrains.annotations.NotNull;

public class LoginActivity extends AppCompatActivity implements ILoginView {
    LoginPresenter presenter;
    TextInputLayout loginInput;
    EditText loginText;
    TextInputLayout passwordInput;
    EditText passwordText;
    Button loginBtn;
    TextView errorTxt;
    ProgressBar loginProgress;
    CheckBox saveCredentialsCb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);

        loginInput = (TextInputLayout) findViewById(R.id.login_til);
        loginText = (EditText) findViewById(R.id.login);
        passwordInput = (TextInputLayout) findViewById(R.id.password_til);
        passwordText = (EditText) findViewById(R.id.password);
        loginBtn = (Button) findViewById(R.id.login_btn);
        errorTxt = (TextView) findViewById(R.id.error);
        loginProgress = (ProgressBar) findViewById(R.id.login_progress);
        saveCredentialsCb = (CheckBox) findViewById(R.id.save_credentials_cb);

        presenter = new LoginPresenter(this,
                new ElmProgram(AndroidSchedulers.mainThread()),
                new AppPrefs(getPreferences(MODE_PRIVATE)),
                ((SampleApp) getApplication()).getService(),
                AndroidSchedulers.mainThread());

        presenter.init();

        presenter.addLoginInput(RxTextView.textChanges(loginText));
        presenter.addPasswordInput(RxTextView.textChanges(passwordText));
        loginBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.loginBtnClick();
            }
        });
        saveCredentialsCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                presenter.onSaveCredentialsCheck(isChecked);
            }
        });


    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        presenter.destroy();
    }


    @Override
    public void error(String error) {
        errorTxt.setText(error);
    }

    @Override
    public void showProgress() {
        loginProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        loginProgress.setVisibility(View.GONE);
    }


    @Override
    public void showPasswordError(String error) {
        passwordInput.setError(error);
    }

    @Override
    public void showLoginError(String error) {
        loginInput.setError(error);
    }

    @Override
    public void showError() {
        errorTxt.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideError() {
        errorTxt.setVisibility(View.GONE);
    }

    @Override
    public void hideLoginError() {
        loginInput.setError("");
    }

    @Override
    public void hidePasswordError() {
        passwordInput.setError("");
    }

    @Override
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(loginText.getWindowToken(), 0);
        }
    }

    @Override
    public void disableLoginBtn() {
        loginBtn.setEnabled(false);
    }

    @Override
    public void enableLoginBtn() {
        loginBtn.setEnabled(true);
    }

    @Override
    public void goToMainScreen() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    @Override
    public void setLogin(@NotNull String login) {
        loginText.setText(login);
    }

    @Override
    public void setPass(@NotNull String pass) {
        passwordText.setText(pass);
    }
}

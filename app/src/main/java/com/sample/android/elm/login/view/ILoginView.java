package com.sample.android.elm.login.view;

public interface ILoginView {
    void error(String errorText);

    void showProgress();

    void hideProgress();

    void showPasswordError(String errorText);

    void showLoginError(String errorText);

    void showError();

    void hideError();

    void hideLoginError();

    void hidePasswordError();

    void hideKeyboard();

    void disableLoginBtn();

    void enableLoginBtn();

    void goToMainScreen();

}

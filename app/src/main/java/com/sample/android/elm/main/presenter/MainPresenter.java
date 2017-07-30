package com.sample.android.elm.main.presenter;

import com.sample.android.elm.main.view.IMainView;

public class MainPresenter {

    private final IMainView view;
    private final String username;

    public MainPresenter(IMainView view, String username) {
        this.view = view;
        this.username = username;
    }


    public void showWelcome() {
        view.showUserWelcome("Hello, " + username + "!");
    }
}

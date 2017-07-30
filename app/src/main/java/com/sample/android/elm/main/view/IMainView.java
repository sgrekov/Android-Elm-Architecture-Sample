package com.sample.android.elm.main.view;

import org.eclipse.egit.github.core.Repository;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IMainView {
    void setTitle(@NotNull String s);

    void showProgress();

    void hideProgress();

    void setErrorText(@NotNull String s);

    void showErrorText();

    void setRepos(@NotNull List<Repository> reposList);
}

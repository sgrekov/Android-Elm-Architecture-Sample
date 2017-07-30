package com.sample.android.elm.data;

import io.reactivex.Single;
import org.eclipse.egit.github.core.Repository;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IApiService {

    Single<Boolean> login(String login, String pass);

    String getUserName();

    Single<List<Repository>> getStarredRepos(@NotNull String userName);
}

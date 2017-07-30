package com.sample.android.elm.data;

import io.reactivex.Single;

public interface IApiService {

    Single<Boolean> login(String login, String pass);
}

package com.sample.android.elm;

import android.app.Application;
import com.sample.android.elm.data.GitHubService;
import io.reactivex.schedulers.Schedulers;

public class SampleApp extends Application {

    GitHubService service;

    @Override
    public void onCreate() {
        super.onCreate();
        service = new GitHubService(Schedulers.io());
    }

    public GitHubService getService() {
        return service;
    }
}

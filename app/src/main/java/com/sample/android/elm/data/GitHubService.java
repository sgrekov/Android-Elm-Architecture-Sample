package com.sample.android.elm.data;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;

import java.util.concurrent.Callable;

public class GitHubService implements IApiService {

    private final Scheduler scheduler;
    private GitHubClient client;

    public GitHubService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Single<Boolean> login(final String login, final String pass) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                client = new GitHubClient();
                client.setCredentials(login, pass);
                UserService userService = new UserService(client);
                return userService.getUser() != null;
            }
        }).subscribeOn(scheduler);
    }
}

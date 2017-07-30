package com.sample.android.elm.data;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.StargazerService;
import org.eclipse.egit.github.core.service.UserService;
import org.jetbrains.annotations.NotNull;

import java.util.List;
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

    @Override
    public String getUserName() {
        return client.getUser();
    }


    @Override
    public Single<List<Repository>> getStarredRepos(@NotNull String userName) {
        return Single.fromCallable(new Callable<List<Repository>>() {
            @Override
            public List<Repository> call() throws Exception {
                StargazerService stargazerService = new StargazerService(client);
                return stargazerService.getStarred();
            }
        }).subscribeOn(scheduler);
    }
}

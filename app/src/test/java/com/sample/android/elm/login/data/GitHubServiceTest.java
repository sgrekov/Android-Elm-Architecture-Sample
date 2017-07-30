package com.sample.android.elm.login.data;

import com.sample.android.elm.data.GitHubService;
import com.sample.android.elm.data.IApiService;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

public class GitHubServiceTest {

    IApiService loginService = new GitHubService(Schedulers.trampoline());


    @Test
    public void login() throws InterruptedException {
//        loginService
//                .login(login, pass)
//                .test()
//                .assertValue(true)
//                .assertComplete();
    }
}

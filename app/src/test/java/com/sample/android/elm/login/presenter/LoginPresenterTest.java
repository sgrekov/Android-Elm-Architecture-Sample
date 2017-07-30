package com.sample.android.elm.login.presenter;

import com.sample.android.elm.data.IApiService;
import com.sample.android.elm.login.view.ILoginView;
import io.reactivex.schedulers.Schedulers;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class LoginPresenterTest {

    LoginPresenter presenter;
    ILoginView view;
    IApiService loginService;

    @Before
    public void setUp() {
        view = Mockito.mock(ILoginView.class);
        loginService = Mockito.mock(IApiService.class);
        presenter = new LoginPresenter(view, loginService, Schedulers.trampoline());
    }

    @Test
    public void validateLogin() {
        Assert.assertEquals(false, presenter.validateLogin(null));
        Assert.assertEquals(false, presenter.validateLogin(""));
        Assert.assertEquals(false, presenter.validateLogin("123"));
        Assert.assertEquals(true, presenter.validateLogin("1234"));
        Assert.assertEquals(true, presenter.validateLogin("1234567891234"));
        Assert.assertEquals(false, presenter.validateLogin("1234567891234567"));
    }

    @Test
    public void validatePass() {
        Assert.assertEquals(false, presenter.validatePass(null));
        Assert.assertEquals(false, presenter.validatePass(""));
        Assert.assertEquals(false, presenter.validatePass("1234"));
        Assert.assertEquals(true, presenter.validatePass("12345"));
        Assert.assertEquals(true, presenter.validatePass("1234567891234"));
    }
}

package org.jdiai.tests;

import org.jdiai.TestInit;
import org.jdiai.flowmodels.LoginFlow;
import org.jdiai.testng.TestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import static org.jdiai.entities.LoginUser.*;

@Listeners(TestNGListener.class)
public class LoginTests implements TestInit {

    @Test
    public void successfulLogin() {
        LoginFlow loginFlow = new LoginFlow();
        loginFlow.successfulLogin();
    }

    @Test
    public void successfulLoginWithGivenUser() {
        LoginFlow loginFlow = new LoginFlow();
        loginFlow.successfulLogin(Roman);
    }

    @Test
    public void emptyUserLogin() {
        LoginFlow loginFlow = new LoginFlow();
        loginFlow.incorrectUserLogin(EmptyUser);
    }

    @Test
    public void nonExistentUserLogin() {
        LoginFlow loginFlow = new LoginFlow();
        loginFlow.incorrectUserLogin(OttoNormalburger);
    }

    @Test
    public void successfulLoginAndLougout() {
        LoginFlow loginFlow = new LoginFlow();
        loginFlow.successfulLoginAndLogout();
    }
}

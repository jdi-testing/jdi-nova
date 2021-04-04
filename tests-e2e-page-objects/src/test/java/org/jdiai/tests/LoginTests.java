package org.jdiai.tests;

import org.jdiai.TestInit;
import org.jdiai.testng.TestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import static org.jdiai.entities.LoginUser.*;

@Listeners(TestNGListener.class)
public class LoginTests implements TestInit {

    @Test
    public void successfulLogin() {
        loginFlow.successfulLogin();
    }

    @Test
    public void successfulLoginWithGivenUser() {
        loginFlow.successfulLogin(Roman);
    }

    @Test
    public void emptyUser() {
        loginFlow.incorrectUserLogin(EmptyUser);
    }

    @Test
    public void nonExistentUser() {
        loginFlow.incorrectUserLogin(OttoNormalburger);
    }

    @Test
    public void userHasTooLongName() {
        loginFlow.incorrectUserLogin(LongNameUser);
    }

    @Test
    public void successfulLoginAndLogout() {
        loginFlow.successfulLoginAndLogout();
    }
}

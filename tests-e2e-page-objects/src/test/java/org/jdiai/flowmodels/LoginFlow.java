package org.jdiai.flowmodels;

import org.jdiai.entities.LoginUser;

import static org.jdiai.JSTalk.loginAs;
import static org.jdiai.asserts.Conditions.have;
import static org.jdiai.asserts.Conditions.text;
import static org.jdiai.entities.LoginUser.*;
import static org.jdiai.site.JDISite.seleniumHomePage;
import static org.jdiai.states.States.*;

public class LoginFlow {

    public void successfulLogin() {
        logout();
        atHomePage();
        seleniumHomePage.userIcon.click();
        loginAs(Roman);
    }

    public void successfulLogin(LoginUser loginUser) {
        logout();
        atHomePage();
        seleniumHomePage.userIcon.click();
        loginAs(loginUser);
    }

    public void incorrectUserLogin(LoginUser loginUser) {
        logout();
        atHomePage();
        seleniumHomePage.userIcon.click();
        loginAs(loginUser);
        seleniumHomePage.loginFailedText.should(have(text("* Login Failed")));
    }

    public void successfulLoginAndLogout() {
        logout();
        atHomePage();
        seleniumHomePage.userIcon.click();
        loginAs(Roman);
        logout();
    }

}

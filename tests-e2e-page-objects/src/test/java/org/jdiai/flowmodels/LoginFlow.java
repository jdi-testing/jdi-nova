package org.jdiai.flowmodels;

import org.jdiai.entities.LoginUser;

import static org.jdiai.JDI.loginAs;
import static org.jdiai.asserts.Conditions.have;
import static org.jdiai.asserts.Conditions.text;
import static org.jdiai.entities.LoginUser.Roman;
import static org.jdiai.site.JDISite.homePage;
import static org.jdiai.states.States.atHomePage;
import static org.jdiai.states.States.logout;

public class LoginFlow {

    public void successfulLogin() {
        logout();
        atHomePage();
        homePage.userIcon.click();
        loginAs(Roman);
    }

    public void successfulLogin(LoginUser loginUser) {
        logout();
        atHomePage();
        homePage.userIcon.click();
        loginAs(loginUser);
    }

    public void incorrectUserLogin(LoginUser loginUser) {
        logout();
        atHomePage();
        homePage.userIcon.click();
        loginAs(loginUser);
        homePage.loginFailedText.should(have(text("* Login Failed")));
    }

    public void successfulLoginAndLogout() {
        logout();
        atHomePage();
        homePage.userIcon.click();
        loginAs(Roman);
        logout();
    }

}

package org.jdiai.tests;

import org.jdiai.TestInit;
import org.jdiai.entities.User;
import org.jdiai.locators.By;
import org.jdiai.testng.TestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.jdiai.JSTalk.$;
import static org.jdiai.JSTalk.openPage;
import static org.jdiai.entities.User.Triss;
import static org.jdiai.states.States.atHomePage;
import static org.jdiai.states.States.logout;
import static org.testng.AssertJUnit.assertEquals;

@Listeners(TestNGListener.class)
public class FillFormKissTest implements TestInit {
    @Test
    public void loginTest() {
        logout();
        atHomePage();
        $("#user-icon").click();
        loginAs(User.Roman);
        openPage("/contacts.html");
        fillContacts(Triss);
        $(By.text("Submit")).click();
        String lastNameInLog = $(".lname-res").getText();
        String descriptionInLog = $(".descr-res").getText();
        assertEquals(lastNameInLog, "Last Name: " + Triss.lastName);
        assertEquals(descriptionInLog, "Description: " + Triss.description);
    }

    private void loginAs(User user) {
        $("#name").input(user.name);
        $("#password").input(user.password);
        $(".fa-sign-in").click();
    }
    private void fillContacts(User user) {
        $("#first-name").input(user.name);
        $("#last-name").input(user.lastName);
        if (user.hasPassport) {
            $("#passport").click();
        }
        $("#passport-number").input(user.passportNumber);
        $("#passport-seria").input(user.passportSeria);
        $("#position").input(user.position);
        $("#gender").selectByName(user.gender);
        $("#religion").input(user.religion);
        $("#weather button").click();
        $("#weather li").select(user.weather);
        $("#description").input(user.description);

    }
}

package org.jdiai.tests;

import org.jdiai.TestInit;
import org.testng.annotations.Test;

import static org.jdiai.entities.User.Triss;

public class ContactTests implements TestInit {

    @Test
    public void successfullyCreateContact() {
        loginFlow.successfulLogin();
        contactFlow.successfullyCreateContact(Triss);
    }

}

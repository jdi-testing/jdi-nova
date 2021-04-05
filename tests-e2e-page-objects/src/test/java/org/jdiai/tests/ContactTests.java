package org.jdiai.tests;

import org.jdiai.TestInit;
import org.jdiai.entities.Contacts;
import org.testng.annotations.Test;

public class ContactTests implements TestInit {

    @Test
    public void successfullyCreateContact() {
        loginFlow.successfulLogin();
        contactFlow.successfullyCreateContact(Contacts.Triss);
    }

}

package org.jdiai.flowmodels;

import org.jdiai.entities.Contacts;

import static org.jdiai.asserts.Conditions.have;
import static org.jdiai.asserts.Conditions.text;
import static org.jdiai.site.JDISite.contactPage;

public class ContactFlow {

    Contacts contact = new Contacts();

    public void successfullyCreateContact(Contacts contact) {
        this.contact = contact;
        contactPage.open();
        contactPage.contactForm.fill(this.contact);
        contactPage.contacts.weatherRain.click();
        contactPage.summaryValue3.click();
        contactPage.summaryValue6.click();
        contactPage.contacts.submitButton.click();
        validateLastNameInResultLog();
        validateDescriptionInResultLog();
        validateWeatherInResultLog();
        validateSummaryInResultLog();
    }

    public void validateLastNameInResultLog() {
        contactPage.lastNameInResult.should(have(text("Last Name: " + contact.lastName)));
    }

    // TODO: implement
//    public void validatePassportConditionInLog() {
//        contactPage.passportInLog.should(have(text("Passport: condition changed to " + contact.hasPassport)));
//    }

    // TODO: implement weather selection for contact
    // TODO: BUG: It is shown as Vegetables although it should be Weather
    public void validateWeatherInResultLog() {
        contactPage.weatherInResult.should(have(text("Vegetables: " + contact.weather)));
    }

    public void validateDescriptionInResultLog() {
        contactPage.descriptionInResult.should(have(text("Description: " + contact.description)));
    }

    public void validateSummaryInResultLog() {
        contactPage.summaryInResult.should(have(text("Summary: " + contact.summary)));
    }
}

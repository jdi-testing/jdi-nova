package org.jdiai.flowmodels;

import org.jdiai.entities.User;

import static org.jdiai.asserts.Conditions.have;
import static org.jdiai.asserts.Conditions.text;
import static org.jdiai.site.JDISite.contactPage;

public class ContactFlow {
    public void successfullyCreateContact(User contact) {
        contactPage.open();
        contactPage.contacts.fillContacts(contact);
        contactPage.contacts.submitButton.click();
        contactPage.summary.select(contact.odd+"", contact.even+"");
        contactPage.contacts.calculateButton.click();
        validateLastNameInResultLog(contact);
        validateDescriptionInResultLog(contact);
        validateWeatherInResultLog(contact);
        validateSummaryInResultLog(contact);
    }

    public void validateLastNameInResultLog(User contact) {
        contactPage.lastNameInResult.should(have(text("Last Name: " + contact.lastName)));
    }

    // TODO: implement
//    public void validatePassportConditionInLog() {
//        contactPage.passportInLog.should(have(text("Passport: condition changed to " + contact.hasPassport)));
//    }

    // TODO: implement weather selection for contact
    // TODO: BUG: It is shown as Vegetables although it should be Weather
    public void validateWeatherInResultLog(User contact) {
        contactPage.weatherInResult.should(have(text("Vegetables: " + contact.weather)));
    }

    public void validateDescriptionInResultLog(User contact) {
        contactPage.descriptionInResult.should(have(text("Description: " + contact.description)));
    }

    public void validateSummaryInResultLog(User contact) {
        contactPage.summaryInResult.should(have(text("Summary: " + (contact.odd + contact.even))));
    }
}

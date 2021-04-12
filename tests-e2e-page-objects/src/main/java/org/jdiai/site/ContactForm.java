package org.jdiai.site;

import org.jdiai.JS;
import org.jdiai.Section;
import org.jdiai.annotations.UI;
import org.jdiai.entities.User;

import static org.jdiai.JDI.$;

public class ContactForm extends Section {
    @UI("#first-name") public JS firstName;
    @UI("#last-name") public JS lastName;
    @UI("#passport-number") public JS passportNumber;
    @UI("#passport-seria") public JS passportSeria;
    @UI("#passport") public JS passport;
    @UI("#position") public JS position;
    @UI("#gender") public JS gender;
    @UI("#religion") public JS religion;
    @UI("#description") public JS description;
    @UI("//button[text()='Submit']") public JS submitButton;
    @UI("//button[text()='Calculate']") public JS calculateButton;

    public void fillContacts(User user) {
        firstName.input(user.name);
        lastName.input(user.lastName);
        passportNumber.input(user.passportNumber);
        passportSeria.input(user.passportSeria);
        position.input(user.position);
        passport.check(user.hasPassport);
        gender.selectByName(user.gender);
        religion.input(user.religion);
        description.input(user.description);

        $("#weather button").click();
        $("#weather ul").select(user.weather.split(", "));

    }
}

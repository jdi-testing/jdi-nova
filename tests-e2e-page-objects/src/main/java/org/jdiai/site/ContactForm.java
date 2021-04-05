package org.jdiai.site;

import org.jdiai.JS;
import org.jdiai.Section;
import org.jdiai.annotations.UI;

public class ContactForm extends Section {
    @UI("[type=submit]") public JS submitButton;
    @UI("first-name") public JS firstNameField;
    @UI("dropdown-menu") public JS weatherDropDown;
}

package org.jdiai.site;

import org.jdiai.JS;
import org.jdiai.Section;
import org.jdiai.annotations.UI;

public class ContactForm extends Section {
    @UI("[type=submit]") public JS submitButton;
    @UI("first-name") public JS firstNameField;
    @UI("dropdown-menu") public JS weatherDropDown;
    //TODO: find a better locator, user weatherDropDown instead
    @UI("div.dropdown > ul:nth-child(2) > li:nth-child(1) > a:nth-child(1) > label:nth-child(2)") public JS weatherRain;
}

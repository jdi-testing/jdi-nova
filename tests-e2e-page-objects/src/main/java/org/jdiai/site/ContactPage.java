package org.jdiai.site;

import org.jdiai.JS;
import org.jdiai.WebPage;
import org.jdiai.annotations.UI;

public class ContactPage extends WebPage {
    @UI(".summ-res") public JS summaryInResult;
    @UI(".lname-res") public JS lastNameInResult;
    @UI(".descr-res") public JS descriptionInResult;
    @UI(".sal-res") public JS weatherInResult;
    public JS contactForm;

    @UI("#contact-form") public ContactForm contacts;

    @UI("section.horizontal-group:nth-child(1) > p:nth-child(2) > label:nth-child(2)") public JS summaryValue3;
    @UI("section.horizontal-group:nth-child(2) > p:nth-child(3) > label:nth-child(2)") public JS summaryValue6;
}

package org.jdiai.entities;

import com.epam.jdi.tools.DataClass;
import org.jdiai.annotations.UI;

public class Contacts extends DataClass<Contacts> {
    public static Contacts Triss = new Contacts().set(u -> {
        u.name = "Triss";
        u.lastName = "Merigold";
        u.description = "Triss Merigold of Maribor was a legendary Temerian sorceress of the 13th century. Called Fourteenth of the Hill by her contemporaries because she was erroneously thought to have been killed during the Battle of Sodden Hill, she passed into history as Merigold the Fearless.";
        u.gender = "Female";
        u.passportNumber = "1354";
        u.passportSeria = "456765";
        u.position = "456765";
        u.religion = "Agnostic";
        u.hasPassport = true;
    });

    @UI("#first-name") public String name;
    @UI public String lastName;
    @UI public String passportNumber;
    @UI public String passportSeria;
    @UI public String position;
    @UI public String description;
    @UI public String gender;
    @UI public String religion;
    @UI("#passport") public Boolean hasPassport;
    @UI("#summary-block") public int summary;
}

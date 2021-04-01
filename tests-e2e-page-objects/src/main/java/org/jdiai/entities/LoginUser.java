package org.jdiai.entities;

import com.epam.jdi.tools.DataClass;
import org.jdiai.annotations.UI;

public class LoginUser extends DataClass<LoginUser> {
    public static LoginUser Roman = new LoginUser().set(
        u -> {u.name = "Roman"; u.password = "Jdi1234"; });

    public static LoginUser OttoNormalburger = new LoginUser().set(
            u -> {u.name = "OttoNormalburger"; u.password = "Jdi1234"; });

    public static LoginUser EmptyUser = new LoginUser().set(
            u -> {u.name = ""; u.password = ""; });

    public static LoginUser LongNameUser = new LoginUser().set(
            u -> {u.name = "hsgfjghqwaefgudshsgfjghqwaefgudsiugbwabDLusdazhiuhwighdsauigdsagiugbewdbgiugbwabDLusdazhihsgfjghqwaefgudsiugbwabDLusdazhiuhwighdsauigdsagiugbewdbguhwighdsauigdsagiugbewdbghsgfjghqwaefgudsiugbwabDLusdazhiuhwighdsauigdsagiugbewdbghsgfjghqwaefgudsiugbwabDLusdazhiuhhsgfjghqwaefgudsiugbwabDLusdazhiuhwighdsauigdsagiugbewdbgwighdsauigdsagiugbewdbghsgfjghqwaefgudsiugbwabDLusdazhiuhwighdsauigdsagiugbewdbghsgfjghqwaefgudsiugbwabDLusdazhiuhwighdsauigdsagiugbewdbghsgfjghqwaefgudsiugbwabDLusdazhiuhwighdsauigdsagiugbewdbghsgfjghqwaefgudsiugbwabDLusdazhiuhwighdsauigdsagiugbewdbghsgfjghqwaefgudsiugbwabDLusdazhiuhwighdhsgfjghqwaefgudsiugbwabDLusdazhiuhwighdsauigdsagiugbewdbgsauigdsagiugbewdbghsgfjghqwaefgudsiugbwabDLusdazhiuhwighdsauigdsagiugbewdbg"; u.password = ""; });

    @UI public String name;
    @UI public String password;

}

package com.chenjiabao.open.chenille.model.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleCheckPassword {
    private int min = 6;
    private int max = 16;
    private int level = 4;
    private String specialChars = "!@#$%^&*()_+|<>?{}[]=-~";

    public void setSpecialChars(String specialChars) {
        if (specialChars != null && !specialChars.trim().isEmpty()){
            this.specialChars = specialChars;
        }else {
            this.specialChars = "!@#$%^&*()_+";
        }
    }
}

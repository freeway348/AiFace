package edu.kust.dto;

import edu.kust.entity.User;

public class RegisterUser extends User {
    private String imageBase64;
    private String code;

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

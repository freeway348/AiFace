package edu.kust.dto;

import edu.kust.entity.User;

public class LoginFace extends User {
    private String imagetoken;

    @Override
    public String getImagetoken() {
        return imagetoken;
    }

    @Override
    public void setImagetoken(String imagetoken) {
        this.imagetoken = imagetoken;
    }
}

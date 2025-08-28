package edu.kust.util;

import java.util.Random;

public class VerificationCode {

    public static String Veri(){
        Random random = new Random();
        int Ver = random.nextInt(9000) + 1000;

        return String.valueOf(Ver);
    }

}

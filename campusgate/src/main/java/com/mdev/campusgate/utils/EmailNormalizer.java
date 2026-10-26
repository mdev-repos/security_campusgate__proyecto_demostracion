package com.mdev.campusgate.utils;

import java.util.Locale;

public class EmailNormalizer {
    public static String normalize(String email){
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
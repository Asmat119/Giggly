package com.wit.giggly;

import android.service.autofill.UserData;

public class RegisterCache {


        private static String firstname;
    private static String secondname;
    private static String username;
    private static String password;
    private static String email;
    private static String confirmpassword;





    public static String getregisterFirstname() {
        return firstname;
    }

    public static void setregisterFirstname(String firstname1) {
        firstname = firstname1;
    }

    public static String getregisterEmail() {
        return email;
    }

    public static void setregisterEmail(String email1) {
        email = email1;
    }

    // Getter and setter for secondname
    public static String getregisterSecondname() {
        return secondname;
    }

    public static void setregisterSecondname(String secondname1) {
        secondname = secondname1;
    }

    // Getter and setter for username
    public static String getregisterUsername() {
        return username;
    }

    public static void setregisterUsername(String username1) {
       username = username1;
    }


    public static String getregisterPassword() {
        return password;
    }

    public static void setregisterPassword(String password1) {
        password = password1;
    }


    public static String getregisterConfirmpassword() {
        return confirmpassword;
    }

    public static void setregisterConfirmpassword(String confirmpassword1) {
        confirmpassword = confirmpassword;
    }

    public static void clearFirstnameCache() {
        firstname = null;
    }

    public static void clearEmail() {
        email = null;
    }

    // Method to clear the cache for secondname
    public static void clearSecondnameCache() {
        secondname = null;
    }

    // Method to clear the cache for username
    public static void clearUsernameCache() {
        username = null;
    }

    // Method to clear the cache for password
    public static void clearPasswordCache() {
        password = null;
    }

    // Method to clear the cache for confirmpassword
    public static void clearConfirmpasswordCache() {
        confirmpassword = null;
    }

}

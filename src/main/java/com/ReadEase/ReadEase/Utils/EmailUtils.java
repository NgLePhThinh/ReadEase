package com.ReadEase.ReadEase.Utils;

public class EmailUtils {
    public static String getEmailMessage(String name, String host, String token){
        return "Hello,\n\n" +
        " We have sent you this email in response to your request to reset your password on ${site-name}." +
        " After you reset your password, any credit card information stored in My Account will be deleted as a security measure\n\n" +
        " To reset your password for ReadEase App, please follow the link below:\n" + getVerificationUrl(host, token) +
        "\n\nThe support Team.";
    }
    public static String getVerificationUrl(String clientHost, String token) {
        return clientHost + "/forgot-password?token=" + token;
    }

}

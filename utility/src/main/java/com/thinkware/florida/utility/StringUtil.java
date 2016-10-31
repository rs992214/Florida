package com.thinkware.florida.utility;

/**
 * Created by Mihoe on 2016-09-19.
 */
public class StringUtil {

    public static String[] getDividedPhonenumber(String phonenumber) {
        String[] numbers = new String[3];

        if (phonenumber.length() == 11) {
            numbers[0] = phonenumber.substring(0, 3);
            numbers[1] = phonenumber.substring(3, 7);
            numbers[2] = phonenumber.substring(7, 11);
        } else if (phonenumber.length() == 10) {
            numbers[0] = phonenumber.substring(0, 3);
            numbers[1] = phonenumber.substring(3, 6);
            numbers[2] = phonenumber.substring(6, 10);
        } else if (phonenumber.length() == 12) {
            numbers[0] = phonenumber.substring(0, 3);
            numbers[1] = phonenumber.substring(4, 8);
            numbers[2] = phonenumber.substring(8, 12);
        }

        return numbers;
    }

    public static String[] getDividedIP(String ipAddress) {
        String[] ips  = ipAddress.split("\\.");

        return ips;
    }
}

package com.external.inomarkastore.util;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@NoArgsConstructor(access = PRIVATE)
public class ValidationUtils {

    public static String formatAndValidatePhoneNumber(String data) {
        data = data.trim();
        if (data.startsWith("8")) {
            data = data.replaceFirst("8", "+7");
        }
        final var formattedPhoneNumber = data.replaceAll("[^+\\d]", "");

        if (data.length() != 12) {
            throw new IllegalArgumentException("Wrong phone number!");
        }
        return formattedPhoneNumber;
    }

    public static String formatAndValidateVinNumber(String data) {
        if (!isNotBlank(data) ||
                data.trim().length() != 17 ||
                !data.trim().toUpperCase().matches("^[A-HJPR-Z\\d]{3}[A-HJPR-Z\\d]{6}[\\dABCDEFGHJKLMNPRSTUVWXYZ]{8}$")) {
            throw new IllegalArgumentException("Wrong vin format");
        } else {
            return data.trim().toUpperCase();
        }
    }

    public static String formatAndValidateEmail(String data) {
        if (!isNotBlank(data) ||
                !data.trim().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Wrong email format");
        } else {
            return data.trim();
        }
    }
}

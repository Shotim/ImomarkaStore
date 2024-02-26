package com.external.inomarkastore.util;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@NoArgsConstructor(access = PRIVATE)
public class StringDbFieldUtils {

    private static final int MAX_STRING_LENGTH = 250;

    public static String scaleString(String data) {
        if (isBlank(data)) {
            return data;
        }
        return data.length() > MAX_STRING_LENGTH ?
                data.substring(0, MAX_STRING_LENGTH - 1) :
                data;
    }
}

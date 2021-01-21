package com.smartosc.training.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHandler {

    private StringHandler() {
        throw new IllegalStateException("Utility class");
    }

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-+]+(.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(.[A-Za-z0-9]+)*(.[A-Za-z]{2,})$";

    public static boolean isExist(String value, List<String> items) {
        for(String item : items) {
            if(StringUtils.normalizeSpace(value)
                    .equalsIgnoreCase(StringUtils.normalizeSpace(item)))
                return true;
        }
        return false;
    }

    public static boolean isEmail(String value) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(StringUtils.normalizeSpace(value));
        return matcher.matches();
    }

    public static boolean isDateTime(String value, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            sdf.parse(StringUtils.normalizeSpace(value));
        } catch (ParseException ex) {
            return false;
        }
        return true;
    }


}

package cecs429.text;

public class Sanitizer {

    public static String sanitize(String value) {
        return value.trim().replaceAll("\t", " ").replaceAll(" +", " ");
    }

}
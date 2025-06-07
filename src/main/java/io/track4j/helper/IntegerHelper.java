package io.track4j.helper;

public final class IntegerHelper {

    private IntegerHelper() {}

    public static int parseInt(String strNum) {
        if (strNum == null || "0".equals(strNum)) {
            return 0;
        }
        try {
            int result = Integer.parseInt(strNum);
            return Math.max(result, 0);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }
}

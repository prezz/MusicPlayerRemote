package net.prezz.mpr;

public class Utils {

    @SuppressWarnings("unchecked")
    public static<T> T cast(Object o) {
        return (T)o;
    }

    public static boolean equals(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    public static int hashCode(Object obj) {
        return obj != null ? obj.hashCode() : 0;
    }

    public static int shortHashCode(Object... values) {
        int hash = 0;
        for (Object v : values) {
            hash ^= v.hashCode();
        }

        hash &= 0x7FFFFFFF;
        int l = (hash >> 16);
        int r = (hash & 0xFFFF);

        return (l ^ r);
    }

    public static String fixDatabaseQuery(String input) {
        if (input.contains("'")) {
            StringBuilder sb = new StringBuilder();
            for (char c : input.toCharArray()) {
                if ('\'' == c) {
                    sb.append("\'");
                }
                sb.append(c);
            }

            return sb.toString();
        }
        return input;
    }

    public static boolean nullOrEmpty(String value) {
        return value == null || "".equals(value);
    }

    public static boolean isLocalHost(String host) {
        return "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host);
    }

    public static String moveInsignificantWordsLast(String input) {
        if (input != null) {
            if (input.toLowerCase().startsWith("the ")) {
                String the = input.substring(0, 3);
                String remaining = input.substring(4);
                return remaining + ", " + the;
            }
        }

        return input;
    }
}

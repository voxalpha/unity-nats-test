public final class Utils {

    private Utils() {
    }

    /**
     * humanBytes formats bytes as a human readable string.
     *
     * @param bytes the number of bytes
     * @return a string representing the number of bytes in human readable string
     */
    public static String humanBytes(double bytes) {
        int base = 1024;
        String[] pre = new String[]{"k", "m", "g", "t", "p", "e"};
        String post = "b";
        if (bytes < (long) base) {
            return String.format("%.2f b", bytes);
        }
        int exp = (int) (Math.log(bytes) / Math.log(base));
        int index = exp - 1;
        String units = pre[index] + post;
        return String.format("%.2f %s", bytes / Math.pow((double) base, (double) exp), units);
    }
}
package backend.utils;

public class MathUtils {

    // Int
    public static long sum(int[] arr) {
        long sum = 0;
        for (int d : arr) {
            sum += d;
        }
        return sum;
    }

    public static long square_sum(int[] arr) {
        long sum = 0;
        for (int d : arr) {
            sum += (long)d*(long)d;
        }
        return sum;
    }

    public static double mean(int[] arr) {
        long sum = 0;
        int count = 0;
        for (long d : arr) {
            sum += d;
            count += 1;
        }
        return (double)sum/count;
    }

    public static double std(int[] arr) {
        long sum = 0;
        double square_sum = 0;
        int count = arr.length;
        for (double d : arr) {
            sum += d;
            square_sum += d*d;
        }
        return Math.sqrt( square_sum/count - ((double)sum/count)*((double)sum/count) );
    }

    // Long
    public static long sum(long[] arr) {
        long sum = 0;
        for (long d : arr) {
            sum += d;
        }
        return sum;
    }

    public static double square_sum(long[] arr) {
        double sum = 0;
        for (long d : arr) {
            sum += d*d;
        }
        return sum;
    }

    public static double mean(long[] arr) {
        long sum = 0;
        int count = 0;
        for (long d : arr) {
            sum += d;
            count += 1;
        }
        return (double)sum/count;
    }

    public static double std(long[] arr) {
        long sum = 0;
        double square_sum = 0;
        int count = arr.length;
        for (double d : arr) {
            sum += d;
            square_sum += d*d;
        }
        return Math.sqrt( square_sum/count - ((double)sum/count)*((double)sum/count) );
    }

    // DOUBLES

    public static double sum(double[] arr) {
        double sum = 0;
        for (double d : arr) {
            sum += d;
        }
        return sum;
    }

    public static double square_sum(double[] arr) {
        double sum = 0;
        for (double d : arr) {
            sum += d*d;
        }
        return sum;
    }

    public static double mean(double[] arr) {
        double sum = 0;
        int count = 0;
        for (double d : arr) {
            sum += d;
            count += 1;
        }
        return sum/count;
    }

    public static double std(double[] arr) {
        double sum = 0;
        double square_sum = 0;
        int count = arr.length;
        for (double d : arr) {
            sum += d;
            square_sum += d*d;
        }
        return Math.sqrt( square_sum/count - (sum/count)*(sum/count) );
    }
}

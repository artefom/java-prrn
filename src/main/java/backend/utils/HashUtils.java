package backend.utils;

import sun.misc.DoubleConsts;

import java.util.Collection;

import static java.lang.Double.doubleToRawLongBits;

public class HashUtils {

    // System.String.GetHashCode(): http://referencesource.microsoft.com/#mscorlib/system/string.cs,0a17bbac4851d0d4
// System.Web.Util.StringUtil.GetStringHashCode(System.String): http://referencesource.microsoft.com/#System.Web/Util/StringUtil.cs,c97063570b4e791a
    public static int CombineHashCodes(int[] hashCodes)
    {
        int hash1 = (5381 << 16) + 5381;
        int hash2 = hash1;

        int i = 0;
        for (int hashCode : hashCodes)
        {
            if (i % 2 == 0)
                hash1 = ((hash1 << 5) + hash1 + (hash1 >> 27)) ^ hashCode;
            else
                hash2 = ((hash2 << 5) + hash2 + (hash2 >> 27)) ^ hashCode;

            ++i;
        }

        return hash1 + (hash2 * 1566083941);
    }

    public static long doubleToLongBits(double value) {
        long result = doubleToRawLongBits(value);
        // Check for NaN based on values of bit fields, maximum
        // exponent and nonzero significand.
        if ( ((result & DoubleConsts.EXP_BIT_MASK) ==
                DoubleConsts.EXP_BIT_MASK) &&
                (result & DoubleConsts.SIGNIF_BIT_MASK) != 0L)
            result = 0x7ff8000000000000L;
        return result;
    }

    public static int hash(double value) {
        long bits = doubleToLongBits(value);
        return (int)(bits ^ (bits >>> 32));
    }
}

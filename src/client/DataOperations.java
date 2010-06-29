
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

/**
 *
 * @author Sean
 */
public class DataOperations {

    public static boolean getBoolean(byte[] data, int offset) {
        return (data[offset] == 1);
    }

    public static int getByte(byte[] data, int offset) {
        return data[offset];
    }

    public static int getShort(byte[] data, int offset) {
        int returnShort = 0;
        returnShort += data[offset];
        returnShort += (data[(offset + 1)] << 8);
        return returnShort;
    }

    public static int getInt(byte[] data, int offset) {
        int returnInt = 0;
        returnInt += data[offset];
        returnInt += (data[(offset + 1)] << 8);
        returnInt += (data[(offset + 2)] << 16);
        returnInt += (data[(offset + 3)] << 24);
        return returnInt;
    }

    public static long getLong(byte[] data, int offset) {
        long returnLong = 0;
        returnLong += data[offset];
        returnLong += (data[(offset + 1)] << 8);
        returnLong += (data[(offset + 2)] << 16);
        returnLong += (data[(offset + 3)] << 24);
        returnLong += (data[(offset + 4)] << 32);
        returnLong += (data[(offset + 5)] << 40);
        returnLong += (data[(offset + 6)] << 48);
        returnLong += (data[(offset + 7)] << 56);
        return returnLong;
    }

    public static String getString(byte[] data, int offset, int length) {
        return new String(data, offset, length);
    }

    public static String padString(String stringToPad, int desiredLength) {
        if (stringToPad.length() > desiredLength) {
            return stringToPad.substring(0, desiredLength);
        } else {
            while ((desiredLength - stringToPad.length()) > 0) {
                stringToPad += " ";
            }
            return stringToPad;
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client;

/**
 *
 * @author Sean
 */
public class PacketOperations {

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
        int returnShort = 0;
        returnShort += data[offset];
        returnShort += (data[(offset + 1)] << 8);
        returnShort += (data[(offset + 2)] << 16);
        returnShort += (data[(offset + 3)] << 24);
        returnShort += (data[(offset + 4)] << 32);
        returnShort += (data[(offset + 5)] << 40);
        returnShort += (data[(offset + 6)] << 48);
        returnShort += (data[(offset + 7)] << 56);
        return returnShort;
    }

    public static String getString(byte[] data, int offset, int length) {
        return new String(data, offset, length);
    }
}

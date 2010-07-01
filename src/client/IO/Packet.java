/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client.IO;

/**
 *
 * @author Sean
 */
public class Packet {

    private int packetHeader;
    private byte[] packetData;
    private int packetPosition;

    public Packet(int packetHeader, byte[] packetData) {
        this.packetHeader = packetHeader;
        this.packetData = packetData;
        packetPosition = 0;
    }

    public int getPacketHeader() {
        return packetHeader;
    }

    public boolean getBoolean() {
        return (getByte() == 1);
    }
    public int getByte() {
        int returnByte = 0;
        if ((packetPosition + 1) <= packetData.length) {
            returnByte += packetData[packetPosition++];
        }
        return returnByte;
    }

    public int getShort() {
        int returnValue = 0;
        if ((packetPosition + 2) <= packetData.length) {
            returnValue += packetData[packetPosition++] << 8;
            returnValue += packetData[packetPosition++];
        }
        return returnValue;
    }

    public int getInt() {
        int returnValue = 0;
        if ((packetPosition + 4) <= packetData.length) {
            returnValue += packetData[packetPosition++] << 24;
            returnValue += packetData[packetPosition++] << 16;
            returnValue += packetData[packetPosition++] << 8;
            returnValue += packetData[packetPosition++];
        }
        return returnValue;
    }

    public long getLong() {
        long returnValue = 0;
        if ((packetPosition + 8) <= packetData.length) {
            returnValue += packetData[packetPosition++] << 56;
            returnValue += packetData[packetPosition++] << 48;
            returnValue += packetData[packetPosition++] << 40;
            returnValue += packetData[packetPosition++] << 32;
            returnValue += packetData[packetPosition++] << 24;
            returnValue += packetData[packetPosition++] << 16;
            returnValue += packetData[packetPosition++] << 8;
            returnValue += packetData[packetPosition++];
        }
        return returnValue;
    }

    public byte[] getBytes(int length) {
        byte[] returnBytes = new byte[length];
        if((packetPosition + length) <= packetData.length) {
            for(int currentPosition = 0; currentPosition < returnBytes.length; currentPosition++) {
                returnBytes[currentPosition] = (byte)getByte();
            }
        }
        return returnBytes;
    }
    public String getString() {
        int stringLength = getInt();
        return new String(getBytes(stringLength));
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client.IO;

/**
 *
 * @author Sean
 */
public class PacketBuilder {

    private int packetHeader;
    private byte[] packetData;
    private int currentPosition;

    public PacketBuilder(int packetHeader) {
        this.packetHeader = packetHeader;
        this.packetData = new byte[10000];
    }

    public void writeBoolean(boolean booleanToAdd) {
        writeByte(booleanToAdd ? 1 : 0);
    }

    public void writeByte(int byteToAdd) {
        packetData[(currentPosition++)] = (byte) byteToAdd;
    }

    public void writeShort(int shortToAdd) {
        packetData[(currentPosition++)] = (byte) (shortToAdd >>> 8);
        packetData[(currentPosition++)] = (byte) shortToAdd;
    }

    public void writeInt(int intToAdd) {
        packetData[(currentPosition++)] = (byte) (intToAdd >>> 24);
        packetData[(currentPosition++)] = (byte) (intToAdd >>> 16);
        packetData[(currentPosition++)] = (byte) (intToAdd >>> 8);
        packetData[(currentPosition++)] = (byte) intToAdd;
    }

    public void writeLong(long longToAdd) {
        packetData[(currentPosition++)] = (byte) (longToAdd >>> 56);
        packetData[(currentPosition++)] = (byte) (longToAdd >>> 48);
        packetData[(currentPosition++)] = (byte) (longToAdd >>> 40);
        packetData[(currentPosition++)] = (byte) (longToAdd >>> 32);
        packetData[(currentPosition++)] = (byte) (longToAdd >>> 24);
        packetData[(currentPosition++)] = (byte) (longToAdd >>> 16);
        packetData[(currentPosition++)] = (byte) (longToAdd >>> 8);
        packetData[(currentPosition++)] = (byte) longToAdd;
    }

    public void writeString(String stringToAdd) {
        writeInt(stringToAdd.length());
        writeBytes(stringToAdd.getBytes());
    }

    public void writeBytes(byte[] bytesToAdd) {
        for (byte currentByte : bytesToAdd) {
            writeByte(currentByte);
        }
    }

    public void writeBytes(byte[] bytesToAdd, int offset, int length) {
        for (int currentByte = 0; currentByte < length; currentByte++) {
            writeByte(bytesToAdd[(offset + currentByte)]);
        }
    }

    public byte[] toByteArray() {
        byte[] dataBuffer = new byte[(currentPosition + 3)];
        dataBuffer[0] = (byte) (currentPosition >>> 8);
        dataBuffer[1] = (byte) currentPosition;
        dataBuffer[2] = (byte) packetHeader;
        for (int currentOffset = 0; currentOffset < currentPosition; currentOffset++) {
            dataBuffer[(currentOffset + 3)] = packetData[currentOffset];
        }
        return dataBuffer;
    }

    public Packet toPacket() {
        byte[] dataBuffer = new byte[currentPosition];
        for (int currentOffset = 0; currentOffset < currentPosition; currentOffset++) {
            dataBuffer[currentOffset] = packetData[currentOffset];
        }
        return new Packet(packetHeader, dataBuffer);
    }
}

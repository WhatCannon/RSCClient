package client;

import java.io.IOException;

public class PacketConstruction {

    protected int length;
    public int packetReadCount;
    public int maxPacketReadCount;
    public int packetStart;
    private int packetOffset;
    public byte packetData[];
    public static int packetCommandCount[] = new int[256];
    protected int packetCount;
    public static int packetCommandLength[] = new int[256];
    protected int maxPacketLength;
    protected boolean error;
    protected String errorText;

    public PacketConstruction() {
        packetOffset = 3;
        errorText = "";
        maxPacketLength = 5000;
        error = false;
    }

    public void createPacket(int i) {
        if (packetStart > (maxPacketLength * 4) / 5) {
            try {
                writePacket(0);
            } catch (IOException ioexception) {
                error = true;
                errorText = ioexception.getMessage();
            }
        }
        if (packetData == null) {
            packetData = new byte[maxPacketLength];
        }
        packetData[packetStart + 2] = (byte) i;
        packetData[packetStart + 3] = 0;
        packetOffset = packetStart + 3;
    }

    public void formatPacket() {
        int packetLength = ((packetOffset - packetStart) - 2);
        packetData[packetStart] = (byte) packetLength;
        packetData[packetStart + 1] = (byte) (packetLength >>> 8);
        if (maxPacketLength <= 10000) {
            int k = packetData[packetStart + 2] & 0xff;
            packetCommandCount[k]++;
            packetCommandLength[k] += packetOffset - packetStart;
        }
        packetStart = packetOffset;
    }

    public void finalisePacket() throws IOException {
        formatPacket();
        writePacket(0);
    }

    public boolean containsData() {
        return (packetStart > 0);
    }

    /*
     * Read operations
     * readByte();
     * readShort();
     * readInt();
     * readLong();
     * readPacket();
     */
    public int readByte() throws IOException {
        return readInputStream();
    }

    public int readShort() throws IOException {
        int returnShort = 0;
        returnShort += readByte();
        returnShort += (readByte() << 8);
        return returnShort;
    }

    public int readInt() throws IOException {
        int returnShort = 0;
        returnShort += readByte();
        returnShort += (readByte() << 8);
        returnShort += (readByte() << 16);
        returnShort += (readByte() << 24);
        return returnShort;
    }

    public long readLong() throws IOException {
        long returnShort = 0;
        returnShort += readByte();
        returnShort += (readByte() << 8);
        returnShort += (readByte() << 16);
        returnShort += (readByte() << 24);
        returnShort += (readByte() << 32);
        returnShort += (readByte() << 40);
        returnShort += (readByte() << 48);
        returnShort += (readByte() << 56);
        return returnShort;
    }

    public int readPacket(byte data[]) {
        try {
            packetReadCount++;
            if (maxPacketReadCount > 0 && packetReadCount > maxPacketReadCount) {
                error = true;
                errorText = "time-out";
                maxPacketReadCount += maxPacketReadCount;
                return 0;
            }
            if (length == 0 && inputStreamAvailable() >= 2) {
                length = readInputStream();
                if (length >= 160) {
                    length = (length - 160) * 256 + readInputStream();
                }
            }
            if (length > 0 && inputStreamAvailable() >= length) {
                if (length >= 160) {
                    readInputStream(length, data);
                } else {
                    data[length - 1] = (byte) readInputStream();
                    if (length > 1) {
                        readInputStream(length - 1, data);
                    }
                }
                int readBytes = length;
                length = 0;
                packetReadCount = 0;
                return readBytes;
            }
        } catch (IOException ioexception) {
            error = true;
            errorText = ioexception.getMessage();
        }
        return 0;
    }
    /*
     * Send operations
     * writeByte()
     * writeShort()
     * writeInt()
     * writeLong
     * writeBytes()
     * writeString()
     * writePacket()
     */

    public void writeByte(int i) {
        packetData[packetOffset++] = (byte) i;
    }

    public void writeShort(int shortToAdd) {
        writeByte((byte) shortToAdd);
        writeByte((byte) (shortToAdd >>> 8));
    }

    public void writeInt(int intToAdd) {
        writeByte((byte) intToAdd);
        writeByte((byte) (intToAdd >>> 8));
        writeByte((byte) (intToAdd >>> 16));
        writeByte((byte) (intToAdd >>> 24));
    }

    public void writeLong(long longToAdd) {
        writeByte((byte) longToAdd);
        writeByte((byte) (longToAdd >>> 8));
        writeByte((byte) (longToAdd >>> 16));
        writeByte((byte) (longToAdd >>> 24));
        writeByte((byte) (longToAdd >>> 32));
        writeByte((byte) (longToAdd >>> 40));
        writeByte((byte) (longToAdd >>> 48));
        writeByte((byte) (longToAdd >>> 56));
    }

    public void writeBytes(byte bytes[], int offset, int length) {
        for (int currentOffset = 0; currentOffset < length; currentOffset++) {
            writeByte(bytes[(offset + currentOffset)]);
        }
    }

    public void writeString(String s) {
        writeBytes(s.getBytes(), 0, s.length());
    }

    public void writePacket(int i)
            throws IOException {
        if (error) {
            packetStart = 0;
            packetOffset = 3;
            error = false;
            throw new IOException(errorText);
        }
        packetCount++;
        if (packetCount < i) {
            return;
        }
        if (packetStart > 0) {
            packetCount = 0;
            writeToOutputBuffer(packetData, 0, packetStart);
        }
        packetStart = 0;
        packetOffset = 3;
    }

    /*
     * Overriden methods
     */
    public int readInputStream() throws IOException {
        return 0;
    }

    public void readInputStream(int length, byte[] data) throws IOException {
        readInputStream(length, 0, data);
    }

    public void readInputStream(int length, int offset, byte data[]) throws IOException {
    }

    public int inputStreamAvailable() throws IOException {
        return 0;
    }

    public void writeToOutputBuffer(byte data[], int offset, int length) throws IOException {
    }

    public void closeStream() {
    }
}

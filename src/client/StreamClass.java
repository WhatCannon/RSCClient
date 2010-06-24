/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author Sean
 */
public class StreamClass implements Runnable {

    private Socket clientSocket;
    private InputStream clientInputStream;
    private OutputStream clientOutputStream;
    private Queue<byte[]> packetQueue = new LinkedList<byte[]>();
    private boolean streamRunning = true;

    public StreamClass(Socket clientSocket, GameWindow gameWindow) throws IOException {
        this.clientSocket = clientSocket;
        this.clientInputStream = clientSocket.getInputStream();
        this.clientOutputStream = clientSocket.getOutputStream();
        gameWindow.startThread(this);
    }

    public void run() {
        while (streamRunning) {
            try {
                if (!packetQueue.isEmpty()) {
                    clientOutputStream.write(packetQueue.remove());
                    clientOutputStream.flush();
                }
                Thread.sleep(100);
            }
            catch(IOException IOe) {
                stopStream();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            clientSocket.close();
        } catch (IOException IOe) {
            IOe.printStackTrace();
        }
    }

    public void stopStream() {
        streamRunning = false;
    }

    public void writePacket(byte[] packetToWrite) {
        packetQueue.add(packetToWrite);
    }

    public Packet readPacket() throws IOException {
        if (clientInputStream.available() >= 2) {
            int packetLength = readShort();
            int packetHeader = readByte();
            byte[] packetData = readBytes(packetLength);
            return new Packet(packetHeader, packetData);
        } else {
            return null;
        }
    }

    public int readByte() throws IOException {
        return clientInputStream.read();
    }

    public int readShort() throws IOException {
        int returnShort = 0;
        returnShort += clientInputStream.read();
        returnShort += (clientInputStream.read() << 8);
        return returnShort;
    }

    public int readInt() throws IOException {
        int returnInt = 0;
        returnInt += clientInputStream.read();
        returnInt += (clientInputStream.read() << 8);
        returnInt += (clientInputStream.read() << 16);
        returnInt += (clientInputStream.read() << 24);
        return returnInt;
    }

    public long readLong() throws IOException {
        long returnLong = 0;
        returnLong += clientInputStream.read();
        returnLong += (clientInputStream.read() << 8);
        returnLong += (clientInputStream.read() << 16);
        returnLong += (clientInputStream.read() << 24);
        returnLong += (clientInputStream.read() << 32);
        returnLong += (clientInputStream.read() << 40);
        returnLong += (clientInputStream.read() << 48);
        returnLong += (clientInputStream.read() << 56);
        return returnLong;
    }

    public byte[] readBytes(int count) throws IOException {
        byte[] returnBytes = new byte[count];
        for (int currentByte = 0; currentByte < returnBytes.length; currentByte++) {
            returnBytes[currentByte] = (byte) readByte();
        }
        return returnBytes;
    }
}

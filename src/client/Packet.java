/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

/**
 *
 * @author Sean
 */
public class Packet {

    private int packetHeader;
    private byte[] packetData;

    public Packet(int packetHeader, byte[] packetData) {
        this.packetHeader = packetHeader;
        this.packetData = packetData;
    }

    public int getHeader() {
        return packetHeader;
    }

    public byte[] getPacketData() {
        return packetData;
    }
}

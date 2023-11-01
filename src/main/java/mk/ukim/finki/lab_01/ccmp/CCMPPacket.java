package mk.ukim.finki.lab_01.ccmp;

import java.nio.ByteBuffer;

/**
 * Dumbed-down CCMP Packet structure
 * --------------------------------------------------------------
 * | MAC Header (12 bytes)                                      |
 * |------------------------------------------------------------|
 * | Packet Number (6 bytes)                                    |
 * |------------------------------------------------------------|
 * | Frame Header (6 bytes)                                     |
 * |------------------------------------------------------------|
 * | Data (variable size, depending on payload)                 |
 * |------------------------------------------------------------|
 * | MIC (8 bytes)                                              |
 * |------------------------------------------------------------|
 */
public class CCMPPacket {
    private final byte[] MACHeader;
    private final byte[] packetNumber;
    private final byte[] frameHeader;
    private byte[] data;
    private final byte[] MIC;

    public CCMPPacket(byte[] data, byte[] packetNumber) {
        this.data = data;
        this.packetNumber = packetNumber;

        this.MACHeader = CCMP.generateMACHeader();
        this.frameHeader = CCMP.generateHeader();
        Nonce nonce = CCMP.createNonce(this.MACHeader, this.packetNumber);

        this.MIC = CCMP.calculateMIC(nonce, this.frameHeader, this.data);
        this.data = CCMP.encryptData(data);
    }

    public CCMPPacket(byte[] MACHeader, byte[] packetNumber, byte[] frameHeader, byte[] data, byte[] MIC) {
        this.MACHeader = MACHeader;
        this.packetNumber = packetNumber;
        this.frameHeader = frameHeader;
        this.data = data;
        this.MIC = MIC;
    }


    public byte[] getMACHeader() {
        return MACHeader;
    }

    public byte[] getFrameHeader() {
        return frameHeader;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getMIC() {
        return MIC;
    }

    public byte[] getPacketNumber() {
        return packetNumber;
    }


    public byte[] bytes() {
        byte[] combined = new byte[MACHeader.length + packetNumber.length + frameHeader.length + data.length
                + MIC.length];

        ByteBuffer buffer = ByteBuffer.wrap(combined);
        buffer.put(MACHeader).put(packetNumber).put(frameHeader).put(data).put(MIC);

        return buffer.array();
    }
}

package mk.ukim.finki.lab_01.ccmp;


import java.nio.ByteBuffer;

/**
 * Nonce 104 (bits)
 * --------------------------------------------------------------
 * | Packet number (PN) (48 bit)                                |
 * |------------------------------------------------------------|
 * | Source MAC (48 bits)                                       |
 * |------------------------------------------------------------|
 * | QoS priority (8 bits)                                      |
 * |------------------------------------------------------------|
 */
public class Nonce {
    private final byte[] packetNumber = new byte[6];
    private final byte[] sourceMAC = new byte[6];
    private final byte[] qosPriority = new byte[1];

    public Nonce(byte[] packetNumber, byte[] sourceMAC, byte[] qosPriority) {
        System.arraycopy(packetNumber, 0, this.packetNumber, 0, this.packetNumber.length);
        System.arraycopy(sourceMAC, 0, this.sourceMAC, 0, this.sourceMAC.length);
        System.arraycopy(qosPriority, 0, this.qosPriority, 0, this.qosPriority.length);
    }

    public byte[] bytes() {
        byte[] combined = new byte[packetNumber.length + sourceMAC.length + qosPriority.length];

        ByteBuffer buffer = ByteBuffer.wrap(combined);
        buffer.put(packetNumber).put(sourceMAC).put(qosPriority);

        return buffer.array();
    }
}

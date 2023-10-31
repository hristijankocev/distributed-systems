package mk.ukim.finki.lab_01.ccmp;

import mk.ukim.finki.lab_01.config.CCMPConfig;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

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
    private byte[] MIC;
    private final Nonce nonce;

    public CCMPPacket(byte[] data, byte[] packetNumber) {
        this.data = data;
        this.packetNumber = packetNumber;

        this.MACHeader = generateMACHeader();
        this.frameHeader = generateHeader();
        this.nonce = createNonce();

        calculateMIC();
        encryptData();
    }

    private void calculateMIC() {
        // Padded nonce
        byte[] aesNonce = AES.encrypt(this.nonce.bytes(), CCMPConfig.DATA.getSECRET_KEY());

        int blockSize = 16;
        int numBlocks = this.frameHeader.length / blockSize;

        byte[] chunkResult = aesNonce;
        byte[] xorResult = new byte[16];

        // CBC (Cypher-block chaining)

        // Go through the frame header
        for (int i = 0; i < numBlocks; i++) {
            int offset = i * blockSize;

            // XOR operation
            for (int j = 0; j < blockSize; j++) {
                xorResult[j] = (byte) (this.frameHeader[offset + j] ^ chunkResult[j]);
            }

            chunkResult = AES.encrypt(xorResult, CCMPConfig.DATA.getSECRET_KEY());
        }

        // Go through the data
        numBlocks = this.data.length / blockSize;
        for (int i = 0; i < numBlocks; i++) {
            int offset = i * blockSize;

            // XOR operation
            for (int j = 0; j < blockSize; j++) {
                xorResult[j] = (byte) (this.data[offset + j] ^ chunkResult[j]);
            }

            chunkResult = AES.encrypt(xorResult, CCMPConfig.DATA.getSECRET_KEY());
        }

        // Take only the first 64 bits (left half)
        byte[] mic = new byte[8];
        System.arraycopy(chunkResult, 0, mic, 0, 8);

        this.MIC = mic;
    }

    private void encryptData() {
        byte[] ctrPreload = new byte[3];

        // Pad the data
        this.data = AES.getPaddedMessage(this.data);

        int blockSize = 16;
        int numBlocks = this.data.length / blockSize;

        for (int i = 0; i < numBlocks; i++) {
            int offset = i * blockSize; // Starting index for the current block

            // Encrypt Preload
            byte[] aesPL = AES.encrypt(ctrPreload, CCMPConfig.DATA.getSECRET_KEY());

            // XOR between the Preload and the actual data
            for (int j = 0; j < blockSize; j++) {
                this.data[offset + j] ^= aesPL[j];
            }

            // CTR Preload increment
            incrementByteArray(ctrPreload);
        }
    }

    private Nonce createNonce() {
        byte[] sourceMAC = new byte[6];
        System.arraycopy(this.MACHeader, 0, sourceMAC, 0, 6);

        return new Nonce(this.packetNumber, sourceMAC, new byte[]{0x00});
    }

    private byte[] generateHeader() {
        Random random = new Random();

        int minLength = 4;
        int maxLength = 6;
        int randomLength = random.nextInt(maxLength - minLength + 1) + minLength;

        byte[] header = new byte[6];

        // Fill the first part with random bytes
        random.nextBytes(header);

        // Fill the remaining part with 0s
        Arrays.fill(header, randomLength, 6, (byte) 0);

        return header;
    }

    /**
     * @return Two MAC addresses concatenated
     */
    private byte[] generateMACHeader() {
        byte[] macHeader = new byte[12];

        byte[] firstMAC = randomMACAddress();
        byte[] secondMAC = randomMACAddress();

        System.arraycopy(firstMAC, 0, macHeader, 0, firstMAC.length);
        System.arraycopy(secondMAC, 0, macHeader, 6, secondMAC.length);

        return macHeader;
    }

    private byte[] randomMACAddress() {
        Random rand = new Random();
        byte[] macAddress = new byte[6];

        for (int i = 0; i < 6; i++) {
            macAddress[i] = (byte) rand.nextInt(256);
        }

        return macAddress;
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

    public byte[] bytes() {
        byte[] combined = new byte[MACHeader.length + packetNumber.length + frameHeader.length + data.length
                + MIC.length];

        ByteBuffer buffer = ByteBuffer.wrap(combined);
        buffer.put(MACHeader).put(packetNumber).put(frameHeader).put(data).put(MIC);

        return buffer.array();
    }

    // Increment a byte array by 1
    private static void incrementByteArray(byte[] byteArray) {
        for (int i = byteArray.length - 1; i >= 0; i--) {
            if (byteArray[i] == (byte) 0xFF) {
                byteArray[i] = 0;
            } else {
                byteArray[i] += 0x01;
                break;
            }
        }
    }
}

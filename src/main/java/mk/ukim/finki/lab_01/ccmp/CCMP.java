package mk.ukim.finki.lab_01.ccmp;

import mk.ukim.finki.lab_01.ccmp.exceptions.InvalidCCMPPacketException;
import mk.ukim.finki.lab_01.ccmp.exceptions.MessageIntegrityViolationException;
import mk.ukim.finki.lab_01.config.CCMPConfig;

import java.util.Arrays;
import java.util.Random;

public class CCMP {
    public static CCMPPacket reconstructPacket(byte[] packetBytes) {
        // The minimum size of the received packet can be 48 bytes
        int minPacketSize = 48;
        int MICSize = 8;
        int MACHeaderSize = 12;
        int packetNumberSize = 6;
        int frameHeaderSize = 6;

        if (packetBytes.length < minPacketSize) {
            throw new InvalidCCMPPacketException("The received packet could not be identified as a CCMP packet.");
        }

        byte[] MACHeader = extractBytesChunk(packetBytes, 0, MACHeaderSize);
        byte[] packetNumber = extractBytesChunk(packetBytes, MACHeaderSize, packetNumberSize);
        byte[] frameHeader = extractBytesChunk(packetBytes, MACHeaderSize + packetNumberSize, frameHeaderSize);
        byte[] MIC = extractBytesChunk(packetBytes, packetBytes.length - MICSize, MICSize);

        int dataLength = packetBytes.length - (MACHeader.length + packetNumber.length + frameHeader.length + MIC.length);
        byte[] data = extractBytesChunk(packetBytes, MACHeaderSize + packetNumberSize + frameHeaderSize, dataLength);

        return new CCMPPacket(MACHeader, packetNumber, frameHeader, data, MIC);
    }

    public static byte[] extractBytesChunk(byte[] packetBytes, int offset, int length) {
        byte[] newChunk = new byte[length];

        System.arraycopy(packetBytes, offset, newChunk, 0, length);

        return newChunk;
    }

    public static byte[] calculateMIC(Nonce nonce, byte[] frameHeader, byte[] data) {
        // Padded nonce
        byte[] aesNonce = AES.encrypt(nonce.bytes(), CCMPConfig.DATA.getSECRET_KEY());

        int blockSize = 16;
        int numBlocks = frameHeader.length / blockSize;

        byte[] chunkResult = aesNonce;
        byte[] xorResult = new byte[16];

        // CBC (Cypher-block chaining)
        // Go through the frame header and the clear text data
        chunkResult = doCBC(frameHeader, blockSize, numBlocks, chunkResult, xorResult);
        numBlocks = data.length / blockSize;
        chunkResult = doCBC(data, blockSize, numBlocks, chunkResult, xorResult);

        // Take only the first 64 bits (left half)
        byte[] mic = new byte[8];
        System.arraycopy(chunkResult, 0, mic, 0, 8);

        return mic;
    }

    public static byte[] doCBC(byte[] data, int blockSize, int numBlocks, byte[] chunkResult, byte[] xorResult) {
        String key = CCMPConfig.DATA.getSECRET_KEY();

        for (int i = 0; i < numBlocks; i++) {
            int offset = i * blockSize;

            // XOR operation
            for (int j = 0; j < blockSize; j++) {
                xorResult[j] = (byte) (data[offset + j] ^ chunkResult[j]);
            }

            chunkResult = AES.encrypt(xorResult, key);
        }

        return chunkResult;
    }

    public static byte[] encryptData(byte[] data) {
        byte[] ctrPreload = new byte[3];

        // Pad the data
        data = AES.getPaddedMessage(data);

        int blockSize = 16;
        int numBlocks = data.length / blockSize;

        for (int i = 0; i < numBlocks; i++) {
            int offset = i * blockSize; // Starting index for the current block

            // Encrypt Preload
            byte[] aesPL = AES.encrypt(ctrPreload, CCMPConfig.DATA.getSECRET_KEY());
            // XOR between the Preload and the actual data
            for (int j = 0; j < blockSize; j++) {
                data[offset + j] ^= aesPL[j];
            }

            // CTR Preload increment
            CCMP.incrementByteArray(ctrPreload);
        }

        return data;
    }

    public static byte[] getDecryptedPayload(byte[] packetBytes) {
        CCMPPacket unpacked = CCMP.reconstructPacket(packetBytes);

        // Decrypt by encrypting the encrypted data :D
        byte[] decryptedPayload = CCMP.encryptData(unpacked.getData());
        byte[] withoutPadding = removePadding(decryptedPayload);

        // Calculate the MIC from the received packet
        Nonce nonce = CCMP.createNonce(unpacked.getMACHeader(), unpacked.getPacketNumber());
        byte[] calculatedMIC = calculateMIC(nonce, unpacked.getFrameHeader(), withoutPadding);

        // Check the integrity of the message
        if (!Arrays.equals(unpacked.getMIC(), calculatedMIC)) {
            throw new MessageIntegrityViolationException("The integrity of the received packet was violated!");
        }

        return withoutPadding;
    }

    public static byte[] generateHeader() {
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

    public static Nonce createNonce(byte[] MACHeader, byte[] packetNumber) {
        byte[] sourceMAC = new byte[6];
        System.arraycopy(MACHeader, 0, sourceMAC, 0, 6);

        return new Nonce(packetNumber, sourceMAC, new byte[]{0x00});
    }

    /**
     * @return Two MAC addresses concatenated
     */
    public static byte[] generateMACHeader() {
        byte[] macHeader = new byte[12];

        byte[] firstMAC = randomMACAddress();
        byte[] secondMAC = randomMACAddress();

        System.arraycopy(firstMAC, 0, macHeader, 0, firstMAC.length);
        System.arraycopy(secondMAC, 0, macHeader, 6, secondMAC.length);

        return macHeader;
    }

    public static byte[] randomMACAddress() {
        Random rand = new Random();
        byte[] macAddress = new byte[6];

        for (int i = 0; i < 6; i++) {
            macAddress[i] = (byte) rand.nextInt(256);
        }

        return macAddress;
    }

    public static byte[] removePadding(byte[] data) {
        if (data == null || data.length == 0) {
            return data; // Nothing to trim from an empty array
        }

        int padLength = 0; // Initialize padding length to 0

        // Start from the end of the array and count consecutive zero bytes (padding)
        for (int i = data.length - 1; i >= 0; i--) {
            if (data[i] == 0) {
                padLength++;
            } else {
                break; // Stop counting at the first non-zero byte
            }
        }

        if (padLength > 0) {
            int trimmedLength = data.length - padLength;
            byte[] trimmedData = new byte[trimmedLength];
            System.arraycopy(data, 0, trimmedData, 0, trimmedLength);
            return trimmedData;
        } else {
            return data; // No padding found, return the original data
        }
    }

    // Increment a byte array by 1
    public static void incrementByteArray(byte[] byteArray) {
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

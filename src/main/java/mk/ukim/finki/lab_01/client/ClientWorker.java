package mk.ukim.finki.lab_01.client;

import mk.ukim.finki.lab_01.ccmp.CCMP;
import mk.ukim.finki.lab_01.ccmp.CCMPPacket;
import mk.ukim.finki.lab_01.ccmp.exceptions.InvalidCCMPPacketException;
import mk.ukim.finki.lab_01.ccmp.exceptions.MessageIntegrityViolationException;
import mk.ukim.finki.lab_01.config.CCMPConfig;
import mk.ukim.finki.lab_01.config.ProtoConfig;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Scanner;

public class ClientWorker extends Thread {
    private final InetAddress address;
    private final int serverPort;
    private final DatagramSocket socket;
    boolean listening;

    public ClientWorker(InetAddress address, int serverPort, boolean listening, DatagramSocket socket) {
        this.address = address;
        this.serverPort = serverPort;
        this.listening = listening;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {

            // Max timeout is set to infinite, so we can have concurrent listening and sending of messages
            this.socket.setSoTimeout(0);

            if (listening) {
                System.out.println("Listener thread started on port: " + this.socket.getLocalPort());

                //noinspection InfiniteLoopStatement
                while (true) {
                    byte[] buffer = new byte[ProtoConfig.DATA.getUDP_PACKET_SIZE()];
                    DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);

                    this.socket.receive(incomingPacket);

                    handleMessage(incomingPacket);
                }
            } else {
                System.out.println("Sending thread started on port: " + this.socket.getLocalPort());

                testConnectionToServer();

                while (true) {
                    Scanner scanner = new Scanner(System.in);
                    String message = scanner.nextLine();

                    if (message.equals("end")) {
                        sendMessage(ProtoConfig.DATA.getLOGOUT_MSG());
                        System.out.println("Exiting client...");
                        System.exit(0);
                    }

                    sendMessage(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testConnectionToServer() throws IOException {
        sendMessage(ProtoConfig.DATA.getTEST_CONNECTION_MSG());
    }

    private void sendMessage(String message) throws IOException {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        // Create a CCMP packet if protocol is enabled
        if (CCMPConfig.DATA.isPROTOCOL_ENABLED()) {
            CCMPPacket packet = new CCMPPacket(messageBytes, new byte[6]);
            messageBytes = packet.bytes();
        }

        DatagramPacket dp = new DatagramPacket(messageBytes, messageBytes.length, this.address, this.serverPort);

        this.socket.send(dp);
    }

    private void handleMessage(DatagramPacket packet) {
        Date date = new Date();

        String serverMessage;
        if (CCMPConfig.DATA.isPROTOCOL_ENABLED()) {
            byte[] message = new byte[packet.getLength()];
            System.arraycopy(packet.getData(), 0, message, 0, packet.getLength());

            try {
                byte[] decryptedDataHex = CCMP.getDecryptedPayload(message);
                serverMessage = new String(decryptedDataHex, StandardCharsets.UTF_8);
            } catch (InvalidCCMPPacketException | MessageIntegrityViolationException e) {
                e.printStackTrace();
                return;
            }

        } else {
            serverMessage = new String(packet.getData(), packet.getOffset(), packet.getLength());
        }

        System.out.println(date + " /Server said: \n" + serverMessage);
    }

    public static void main(String[] args) {
        try {
            int serverPort = ProtoConfig.DATA.getSERVER_PORT();
            InetAddress clientAddress = InetAddress.getByName("localhost");

            DatagramSocket socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.bind(null);

            // One thread for writing and one for listening
            ClientWorker writer = new ClientWorker(clientAddress, serverPort, false, socket);
            writer.start();

            ClientWorker listener = new ClientWorker(clientAddress, serverPort, true, socket);
            listener.start();
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
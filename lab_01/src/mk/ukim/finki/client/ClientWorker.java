package mk.ukim.finki.client;

import mk.ukim.finki.config.ProtoConfig;

import java.io.IOException;
import java.net.*;
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

                    byte[] incomingData = incomingPacket.getData();

                    String message = new String(incomingData, incomingPacket.getOffset(), incomingPacket.getLength());

                    handleMessage(message);
                }
            } else {
                System.out.println("Sending thread started on port: " + this.socket.getLocalPort());

                testConnectionToServer(this.socket);

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

    private void testConnectionToServer(DatagramSocket socket) {
        try {
            String testMessage = ProtoConfig.DATA.getTEST_CONNECTION_MSG();
            byte[] messageBytes = testMessage.getBytes();

            DatagramPacket dp = new DatagramPacket(messageBytes, messageBytes.length, this.address, this.serverPort);

            socket.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message) throws IOException {
        byte[] b = message.getBytes();

        DatagramPacket dp = new DatagramPacket(b, b.length, this.address, this.serverPort);

        this.socket.send(dp);
    }

    private void handleMessage(String message) {
        Date date = new Date();
        System.out.println(date + " /Server said: \n" + message);
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
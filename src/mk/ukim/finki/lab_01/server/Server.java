package mk.ukim.finki.lab_01.server;

import mk.ukim.finki.lab_01.config.ProtoConfig;

import java.io.IOException;
import java.net.*;
import java.util.Hashtable;
import java.util.List;

public class Server implements Runnable {
    private final int port;
    private final Hashtable<String, List<String>> users;

    public Server(int port) {
        this.port = port;
        this.users = new Hashtable<>();
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(this.port);

            System.out.println("Server started, listening on port " + this.port);

            //noinspection InfiniteLoopStatement
            while (true) {
                byte[] buffer = new byte[ProtoConfig.DATA.getUDP_PACKET_SIZE()];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);

                // Create a new thread to handle the request, so that we can keep listening to other requests
                new Thread(new ServerWorker(this.users, socket, packet)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        Server server = new Server(ProtoConfig.DATA.getSERVER_PORT());

        new Thread(server).start();
    }
}

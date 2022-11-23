package mk.ukim.finki.lab_02.server;

import mk.ukim.finki.lab_02.classes.Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Hashtable;

public class Server implements Runnable {
    private ServerSocket serverSocket;

    private final Hashtable<String, Client> clients;

    public Server(int port) {
        this.clients = new Hashtable<>();
        try {
            this.serverSocket = new ServerSocket(port);
            System.out.printf("%s Server started on port: %d\n", new Date(), port);
        } catch (IOException e) {
            System.out.printf("%s Failed to start server on port %d\n", new Date(), port);
        }
    }

    @Override
    public void run() {
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                Socket socket = this.serverSocket.accept();

                // Process the request on a new thread of a ServerWorker
                new Thread(new ServerWorker(socket, this.clients)).start();
            }
        } catch (IOException e) {
            System.out.printf("%s Failed to accept a connection on port: %d\n", new Date(), this.serverSocket.getLocalPort());
        }
    }

    public static void main(String[] args) {
        // Server will listen on this port
        int port = 6666;

        // Start the server
        new Thread(new Server(port)).start();
    }
}

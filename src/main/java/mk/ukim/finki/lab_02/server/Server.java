package mk.ukim.finki.lab_02.server;

import mk.ukim.finki.lab_02.classes.Client;
import mk.ukim.finki.lab_02.classes.Job;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.Semaphore;

public class Server implements Runnable {
    private ServerSocket serverSocket;
    private final Hashtable<String, Client> clients;
    // Used to determine which thread is responsible for which TCP tunnel
    protected static Hashtable<Client, Thread> workerThreads;
    // Used for marking that a thread has a job to do
    protected final static Hashtable<Thread, Job> threadJobs = new Hashtable<>();
    // This is a tool that will help us later
    protected final static Semaphore semaphore = new Semaphore(0);

    public Server(int port) {
        Server.workerThreads = new Hashtable<>();

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
                Thread worker = new Thread(new ServerWorker(socket, this.clients));
                worker.start();
                worker.setName(String.valueOf(socket.getPort()));

                // Associate the thread with the client that sent the message
                Client client = new Client(socket.getInetAddress(), socket.getPort());
                Server.workerThreads.put(client, worker);

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

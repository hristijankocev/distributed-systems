package mk.ukim.finki.lab_02.client;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Scanner;

public class Client implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public Client(InetAddress serverAddress, int serverPort) {
        try {
            this.socket = new Socket();
            this.socket.setReuseAddress(true);
            this.socket.connect(new InetSocketAddress(serverAddress, serverPort));
        } catch (IOException e) {
            System.out.printf("Failed to connect to the server \"%s\" on port \"%d\"\n", serverAddress.getHostAddress(), serverPort);
        }
    }

    @Override
    public void run() {
        // First start a listener thread
        startListener();

        System.out.printf("%s Started writer for client.\n", new Date());
        try {
            this.writer = getWriter();
            Scanner scanner = new Scanner(System.in);
            String message;
            while (!(message = scanner.nextLine()).equals("disconnect")) {
                this.writer.println(message);
            }
            this.socket.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void startListener() {
        try {
            this.reader = getReader();
            new Thread(new ClientWriter(this.reader)).start();
        } catch (IOException e) {
            System.out.printf("%s Failed to create a listener thread.\n", new Date());
        }
    }

    private PrintWriter getWriter() throws IOException {
        return new PrintWriter(this.socket.getOutputStream(), true);
    }

    private BufferedReader getReader() throws IOException {
        InputStream inputStream = this.socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        return new BufferedReader(inputStreamReader);
    }


    public static void main(String[] args) {
        try {
            System.out.printf("%s Starting client...\n", new Date());

            int serverPort = 6666;
            InetAddress serverAddress = InetAddress.getByName("127.0.0.1");

            new Thread(new Client(serverAddress, serverPort)).start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
